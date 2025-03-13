package com.user19.pfe_testing.service;
import com.user19.pfe_testing.dto.ProcessInstanceDTO;
import com.user19.pfe_testing.mapper.Mapper;
import com.user19.pfe_testing.model.ApprovalStep;
import com.user19.pfe_testing.model.ProcessInstance;
import com.user19.pfe_testing.model.ProcessStep;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import com.user19.pfe_testing.repository.*;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import com.user19.pfe_testing.util.MathUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class ValidatorService {
    private final ProcessInstanceRepository processInstanceRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;
    private final ProcessStepRepository processStepRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ProcessHistoryRepository processHistoryRepository;
    private final ProcessService processService;
    private final Mapper mapper;


    public List<ProcessInstanceDTO> getAllPendingProcess() {
        var processInstances = processInstanceRepository.findAll();
        List<ProcessInstanceDTO> allProcessInstanceNeedApprove= processInstances.stream().filter((process)->{
            ProcessStep currentStep=getCurrentStepFromProcess(process);
            return isValidatorOfCurrentStep(currentStep,process);
        }).map(mapper::processInstanceToDTO).toList();
        return allProcessInstanceNeedApprove;

    }
    public void Approve(String processInstanceId, String comment ) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(EntityNotFoundException::new);
        ApprovalStep currentApprovalStep = approvalStepRepository.findByName(processInstance.getCurrentStepName())
                .orElseThrow(EntityNotFoundException::new);
        String requiredApproval= currentApprovalStep.getRequiredApproval();
        String actorId= keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();

        switch (requiredApproval) {
            case "ANY":
                processService.addLogsToProcessHistory(processInstance,action,actorId, comment );
                processService.moveNextStep(processInstanceId);
                break;
            case "ALL":
                processService.addLogsToProcessHistory(processInstance,action,actorId, comment );
                int numberOfValidatorWhoValidThisStep=processHistoryRepository.findByProcessInstanceAndAction(processInstance,action).size();
                if(
                        numberOfValidatorWhoValidThisStep==currentApprovalStep.getValidatorRoles().size()
                ){
                    processService.moveNextStep(processInstanceId);
                }

            break;
            default:
                if(MathUtil.isNumeric(requiredApproval)){
                    processService.addLogsToProcessHistory(processInstance,action,actorId, comment );
                    int numberOfValidatorWhoValidThisSteps=processHistoryRepository.findByProcessInstanceAndAction(processInstance,action).size();
                    if(
                            numberOfValidatorWhoValidThisSteps==Integer.parseInt(requiredApproval)
                    ){
                        processService.moveNextStep(processInstanceId);
                    }



                }
        }
    }
    public void reject(String processInstanceId, String comment) {
        ProcessInstance processInstance = processInstanceRepository.findById(processInstanceId).orElseThrow(EntityNotFoundException::new);
        ApprovalStep currentApprovalStep = approvalStepRepository.findByName(processInstance.getCurrentStepName())
                .orElseThrow(EntityNotFoundException::new);
        String actorId= keycloakSecurityUtil.getCurrentUserId();
        String action = currentApprovalStep.getName();
        processInstance.setStatus(ProcessStatus.REJECTED);
        processService.addLogsToProcessHistory(processInstance,action,actorId, comment);


    }

    private ProcessStep getCurrentStepFromProcess(ProcessInstance processInstance) {
        String currentStepId= processInstance.getCurrentStepName();
        return processStepRepository.findByName(currentStepId);
    }
    private boolean isValidatorOfCurrentStep(ProcessStep processStep,ProcessInstance processInstance) {
        if (processStep instanceof ApprovalStep) {
            ApprovalStep approvalStep = (ApprovalStep) processStep;

            Set<String> userRoles = keycloakSecurityUtil.getCurrentUserRoles();

            Set<String> requiredValidatorRoles = new HashSet<>(approvalStep.getValidatorRoles());

            return userRoles.stream().anyMatch(requiredValidatorRoles::contains)
                    && !checkIfValidatorHasApproveThisStep(processStep,processInstance);
        }
        return false;
    }
    private  boolean checkIfValidatorHasApproveThisStep(ProcessStep processStep,ProcessInstance processInstance) {
        String currentUserId = keycloakSecurityUtil.getCurrentUserId();
        ApprovalStep approvalStep = (ApprovalStep) processStep;
        int numberOfStepValidByCurrentUser = processInstance.getHistory()
                .stream().filter(e-> Objects.equals(e.getActorId(), currentUserId)).toList().size();

        return numberOfStepValidByCurrentUser >0;
    }



}

