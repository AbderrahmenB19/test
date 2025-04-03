package com.user19.pfe_testing.mapper;


import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.util.MathUtil;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Mapper {


    public ProcessStep convertStepDTOToEntity(ProcessStepDTO stepDTO, ProcessDefinition processDefinition) {
        switch (stepDTO.getStepType()) {
            case "APPROVAL":
                ApprovalStep approvalStep = new ApprovalStep();
                approvalStep.setName(stepDTO.getName());
                approvalStep.setValidatorRoles(stepDTO.getValidatorRoles());
                String requiredApproval= stepDTO.getRequiredApproval();
                if(!testRequiredApprovalIsValid(requiredApproval)) throw new RuntimeException("BAD VALUE FOR REQUIRED APPROVAL");
                approvalStep.setRequiredApproval(requiredApproval);
                approvalStep.setProcessDefinition(processDefinition);


                return approvalStep;

            case "CONDITION":

                ConditionStep conditionStep = new ConditionStep();
                conditionStep.setName(stepDTO.getName());
                //conditionStep.setConditions( stepDTO.getCondition().stream().map(this::conditionDTOToEntity).toList());
                conditionStep.setProcessDefinition(processDefinition);
                if(stepDTO.getId()!=null) conditionStep.setId(stepDTO.getId());
                return conditionStep;

            case "NOTIFY":

                NotificationStep notificationStep = new NotificationStep();
                notificationStep.setName(stepDTO.getName());
                notificationStep.setRecipients(stepDTO.getRecipients());
                notificationStep.setMessage(stepDTO.getMessage());
                notificationStep.setProcessDefinition(processDefinition);
                if(stepDTO.getId()!=null) notificationStep.setId(stepDTO.getId());
                return notificationStep;

            default:
                throw new IllegalArgumentException("Unknown step type: " + stepDTO.getStepType());
        }
    }
    public ProcessStepDTO convertStepEntityToDTO(ProcessStep step) {

        ProcessStepDTO stepDTO = new ProcessStepDTO();
        stepDTO.setName(step.getName());
        if( step instanceof ApprovalStep){
            stepDTO.setStepType("APPROVAL");
            stepDTO.setRequiredApproval(((ApprovalStep) step).getRequiredApproval());
            stepDTO.setValidatorRoles(((ApprovalStep) step).getValidatorRoles());
        }
        if( step instanceof ConditionStep){
            stepDTO.setStepType("CONDITION");
            stepDTO.setCondition(((ConditionStep) step).getConditions()
                    .stream().map(this::conditionEntityToDTO).toList()
            );

        }
        if( step instanceof NotificationStep){
            stepDTO.setStepType("NOTIFY");
            stepDTO.setMessage(((NotificationStep) step).getMessage());
            stepDTO.setRecipients(((NotificationStep) step).getRecipients());

        }
        return stepDTO;

    }
    public ProcessHistoryDTO processHistoryToDTO(ProcessHistory processHistory) {
        return ProcessHistoryDTO.builder()
                .action(processHistory.getAction())
                .actionStatus(processHistory.getActionStatus())
                .comments(processHistory.getComments())
                .timestamp(processHistory.getTimestamp())
                .build();
    }
    public ProcessInstanceDTO processInstanceToDTO(ProcessInstance processInstance) {
        if (processInstance == null || processInstance.getHistory() == null || processInstance.getHistory().isEmpty()) {
            throw new IllegalArgumentException("Invalid process instance or history is empty.");
        }

        var firstHistory = processInstance.getHistory().getFirst();
        var lastHistory = processInstance.getHistory().getLast();
        String rejectComment = Optional.ofNullable(lastHistory.getComments()).orElse("");

        return ProcessInstanceDTO.builder()
                .id(processInstance.getId())
                .formData(processInstance.getFormData())
                .createdAt(firstHistory.getTimestamp())
                .rejectionComment(rejectComment)
                .build();
    }
    private boolean testRequiredApprovalIsValid(String requiredApproval){
        if(requiredApproval == null || requiredApproval.isEmpty() ){
            return false;
        }
        return requiredApproval.equals("ALL") || requiredApproval.equals("ANY") || MathUtil.isNumeric(requiredApproval);

    }
    public FormSchemaDTO formSchemaToDTO(FormSchema formSchema) {
        return FormSchemaDTO.builder()
                .id(formSchema.getId())
                .jsonSchema(formSchema.getJsonSchema())
                .build();
    }
    public Condition conditionDTOToEntity(ConditionDTO conditionDTO) {
        return Condition.builder()
                .condition(conditionDTO.getCondition())
                .targetStep(conditionDTO.getTargetStep())
                .build();
    }
    public ConditionDTO conditionEntityToDTO(Condition condition) {
        return ConditionDTO.builder()
                .condition(condition.getCondition())
                .targetStep(condition.getTargetStep())
                .build();
    }

}

