package com.user19.pfe_testing.mapper;


import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.util.MathUtil;
import org.springframework.stereotype.Component;

@Component
public class Mapper {


    public ProcessStep convertStepDTOToEntity(ProcessStepDTO stepDTO, ProcessDefinition processDefinition) {
        switch (stepDTO.getStepType()) {

            case "APPROVAL":

                ApprovalStepDTO approvalDTO = (ApprovalStepDTO) stepDTO;
                ApprovalStep approvalStep = new ApprovalStep();
                approvalStep.setName(approvalDTO.getName());
                approvalStep.setValidatorRoles(approvalDTO.getValidatorRoles());
                String requiredApproval= stepDTO.getRequiredApproval();
                if(!testRequiredApprovalIsValid(requiredApproval)) throw new RuntimeException("BAD VALUE FOR REQUIRED APPROVAL");
                approvalStep.setRequiredApproval(requiredApproval);
                approvalStep.setProcessDefinition(processDefinition);
                return approvalStep;

            case "CONDITION":
                ConditionStepDTO conditionDTO = (ConditionStepDTO) stepDTO;
                ConditionStep conditionStep = new ConditionStep();
                conditionStep.setName(conditionDTO.getName());
                conditionStep.setConditions( stepDTO.getCondition());
                conditionStep.setProcessDefinition(processDefinition);
                return conditionStep;

            case "NOTIFY":
                NotificationStepDTO notificationDTO = (NotificationStepDTO) stepDTO;
                NotificationStep notificationStep = new NotificationStep();
                notificationStep.setName(notificationDTO.getName());
                notificationStep.setRecipients(notificationDTO.getRecipients());
                notificationStep.setMessage(notificationDTO.getMessage());
                notificationStep.setProcessDefinition(processDefinition);
                return notificationStep;

            default:
                throw new IllegalArgumentException("Unknown step type: " + stepDTO.getStepType());
        }
    }
    public ProcessHistoryDTO processHistoryToDTO(ProcessHistory processHistory) {
        return ProcessHistoryDTO.builder()
                .action(processHistory.getAction())
                .comments(processHistory.getComments())
                .timestamp(processHistory.getTimestamp())
                .build();
    }
    public ProcessInstanceDTO processInstanceToDTO(ProcessInstance processInstance) {
        return ProcessInstanceDTO.builder()
                .processInstanceId(processInstance.getId())
                .formData(processInstance.getFormData())
                .build();
    }
    private boolean testRequiredApprovalIsValid(String requiredApproval){
        if(requiredApproval == null || requiredApproval.isEmpty() ){
            return false;
        }
        return requiredApproval.equals("ALL") || requiredApproval.equals("ANY") || MathUtil.isNumeric(requiredApproval);

    }
}

