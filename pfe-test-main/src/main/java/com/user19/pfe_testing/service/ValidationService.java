package com.user19.pfe_testing.service;

import com.user19.pfe_testing.dto.ProcessInstanceDTO;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.ApprovalStep;
import com.user19.pfe_testing.model.ProcessHistory;
import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.model.ProcessStep;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import com.user19.pfe_testing.util.MathUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidationService {
    private final ProcessInstanceRepository processInstanceRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final ProcessStepRepository processStepRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final ProcessService processService;
    private final Mapper mapper;


    public List<ProcessInstanceDTO> getAllPendingProcess() {
        return processInstanceRepository.findAll().stream()
                .filter(process -> isValidatorOfCurrentStep(getCurrentStepFromProcess(process), process))
                .map(mapper::processInstanceToDTO)
                .toList();
    }


    public void approve(String processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        ApprovalStep currentApprovalStep = getApprovalStepByName(processInstance.getCurrentStepName());

        String actorId = keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();

        processService.addLogsToProcessHistory(processInstance, action, actorId, "", ProcessStatus.APPROVED);

        if (shouldMoveToNextStep(processInstance, currentApprovalStep, action)) {
            processService.moveNextStep(processInstanceId);
        }
    }


    public void reject(String processInstanceId, String comment) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        ApprovalStep currentApprovalStep = getApprovalStepByName(processInstance.getCurrentStepName());

        String actorId = keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();

        processInstance.setStatus(ProcessStatus.REJECTED);
        processService.addLogsToProcessHistory(processInstance, action, actorId, comment, ProcessStatus.REJECTED);
    }


    public List<ProcessInstanceDTO> getRequestsByStatus(String status) {
        String currentUserId = keycloakSecurityUtil.getCurrentUserId();

        return switch (status.toUpperCase()) {
            case "APPROVED" -> getProcessInstancesByValidatorAndStatus(currentUserId, ProcessStatus.APPROVED);
            case "REJECTED" -> getProcessInstancesByValidatorAndStatus(currentUserId, ProcessStatus.REJECTED);
            case "PENDING" -> getAllPendingProcess();
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };
    }

    private ProcessInstance getProcessInstanceById(String processInstanceId) {
        return processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new EntityNotFoundException("Process instance not found: " + processInstanceId));
    }

    private ApprovalStep getApprovalStepByName(String stepName) {
        return approvalStepRepository.findByName(stepName)
                .orElseThrow(() -> new EntityNotFoundException("Approval step not found: " + stepName));
    }

    private ProcessStep getCurrentStepFromProcess(ProcessInstance processInstance) {
        return processStepRepository.findByName(processInstance.getCurrentStepName());
    }

    private boolean isValidatorOfCurrentStep(ProcessStep processStep, ProcessInstance processInstance) {
        if (processStep instanceof ApprovalStep approvalStep) {
            Set<String> userRoles = keycloakSecurityUtil.getCurrentUserRoles();
            Set<String> requiredValidatorRoles = new HashSet<>(approvalStep.getValidatorRoles());

            return userRoles.stream().anyMatch(requiredValidatorRoles::contains) &&
                    !hasValidatorApprovedStep(processStep, processInstance);
        }
        return false;
    }

    private boolean hasValidatorApprovedStep(ProcessStep processStep, ProcessInstance processInstance) {
        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        return processInstance.getHistory().stream()
                .anyMatch(e -> Objects.equals(e.getActorId(), currentUserId));
    }

    private boolean shouldMoveToNextStep(ProcessInstance processInstance, ApprovalStep approvalStep, String action) {
        int approvalsCount = processHistoryRepository.findByProcessInstanceAndAction(processInstance, action).size();

        return switch (approvalStep.getRequiredApproval()) {
            case "ANY" -> true;
            case "ALL" -> approvalsCount == approvalStep.getValidatorRoles().size();
            default -> MathUtil.isNumeric(approvalStep.getRequiredApproval()) &&
                    approvalsCount == Integer.parseInt(approvalStep.getRequiredApproval());
        };
    }

    private List<ProcessInstanceDTO> getProcessInstancesByValidatorAndStatus(String currentUserId, ProcessStatus status) {
        return processHistoryRepository.findByActorIdAndActionStatus(currentUserId, status).stream()
                .map(ProcessHistory::getProcessInstance)
                .map(mapper::processInstanceToDTO)
                .collect(Collectors.toList());
    }
}
