package com.user19.pfe_testing.service;

import com.user19.pfe_testing.dto.ProcessInstanceDTO;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.*;
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
                .filter(p->p.getStatus() == ProcessStatus.PENDING)
                .filter(process -> isValidatorOfCurrentStep(getCurrentStepFromProcess(process), process))
                .map(e->{
                    ApprovalStep approvalStep = approvalStepRepository.findByNameAndProcessDefinition(e.getCurrentStepName(),e.getProcessDefinition()).orElseThrow(()-> new EntityNotFoundException("ApprovalStep not found"));
                    Long formId = approvalStep.getFormId();
                    return mapper.processInstanceToDTO(e, formId);

                })
                .toList();
    }


    public void approve(Long processInstanceId) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        ApprovalStep currentApprovalStep = getApprovalStepByName(processInstance.getCurrentStepName(), processInstance.getProcessDefinition());

        String actorId = keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();

        processService.addProcessHistory(processInstance, action, actorId, "", ProcessStatus.APPROVED);

        if (shouldMoveToNextStep(processInstance, currentApprovalStep, action)) {
            processService.moveToNextStep(processInstanceId);
        }
    }


    public void reject(Long processInstanceId, String comment) {
        ProcessInstance processInstance = getProcessInstanceById(processInstanceId);
        ApprovalStep currentApprovalStep = getApprovalStepByName(processInstance.getCurrentStepName(), processInstance.getProcessDefinition());

        String actorId = keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();

        processInstance.setStatus(ProcessStatus.REJECTED);
        processService.addProcessHistory(processInstance, action, actorId, comment, ProcessStatus.REJECTED);
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

    private ProcessInstance getProcessInstanceById(Long processInstanceId) {
        return processInstanceRepository.findById(processInstanceId)
                .orElseThrow(() -> new EntityNotFoundException("Process instance not found: " + processInstanceId));
    }

    private ApprovalStep getApprovalStepByName(String stepName , ProcessDefinition processDefinition) {
        return approvalStepRepository.findByNameAndProcessDefinition(stepName, processDefinition)
                .orElseThrow(() -> new EntityNotFoundException("Approval step not found: " + stepName));
    }

    private ProcessStep getCurrentStepFromProcess(ProcessInstance processInstance) {
        return processStepRepository.findByNameAndProcessDefinition(processInstance.getCurrentStepName(), processInstance.getProcessDefinition());
    }

    private boolean isValidatorOfCurrentStep(ProcessStep processStep, ProcessInstance processInstance) {
        if (processStep instanceof ApprovalStep approvalStep) {
            Set<String> userRoles = keycloakSecurityUtil.getCurrentUserRoles();

            Set<String> requiredValidatorRoles = new HashSet<>(approvalStep.getValidatorRoles()).stream().map(e->"ROLE_" + e).collect(Collectors.toSet());


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
                .map(e->{

                    ApprovalStep approvalStep = approvalStepRepository.findByNameAndProcessDefinition(e.getAction(),e.getProcessInstance().getProcessDefinition()).orElseThrow(()->new EntityNotFoundException("Approval step not found" + e.getAction()));
                    Long formId = approvalStep.getFormId();

                    return mapper.processInstanceToDTO(e.getProcessInstance(), formId);
                })

                .collect(Collectors.toList());
    }
}
