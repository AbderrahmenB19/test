package com.user19.pfe_testing.mapper;


import com.user19.pfe_testing.dto.*;
import com.user19.pfe_testing.model.*;
import com.user19.pfe_testing.util.KeycloakSecurityUtil;
import com.user19.pfe_testing.util.MathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Mapper {
    private final KeycloakSecurityUtil keycloakSecurityUtil;


    public ProcessStep convertStepDTOToEntity(ProcessStepDTO stepDTO, ProcessDefinition processDefinition) {
        System.out.println(stepDTO.getFormId());
        switch (stepDTO.getStepType()) {
            case "APPROVAL":
                ApprovalStep approvalStep = new ApprovalStep();
                if (stepDTO.getId()!=null)   approvalStep.setId(stepDTO.getId());
                approvalStep.setFormId(stepDTO.getFormId());
                approvalStep.setName(stepDTO.getName());
                approvalStep.setValidatorRoles(stepDTO.getValidatorRoles());
                String requiredApproval= stepDTO.getRequiredApproval();
                if(!testRequiredApprovalIsValid(requiredApproval)) throw new RuntimeException("BAD VALUE FOR REQUIRED APPROVAL");
                approvalStep.setRequiredApproval(requiredApproval);
                approvalStep.setProcessDefinition(processDefinition);


                return approvalStep;

            case "CONDITION":


                ConditionStep conditionStep = new ConditionStep();
                if (stepDTO.getId()!=null)  conditionStep.setId(stepDTO.getId());
                conditionStep.setFormId(stepDTO.getFormId());
                conditionStep.setName(stepDTO.getName());

                conditionStep.setProcessDefinition(processDefinition);
                if(stepDTO.getId()!=null) conditionStep.setId(stepDTO.getId());
                return conditionStep;

            case "NOTIFY":

                NotificationStep notificationStep = new NotificationStep();
                if (stepDTO.getId()!=null)  notificationStep.setId(stepDTO.getId());
                notificationStep.setName(stepDTO.getName());
                notificationStep.setFormId(stepDTO.getFormId());
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
        if(step.getId()!=null) stepDTO.setId(step.getId());
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
    public ProcessInstanceDTO processInstanceToDTO(ProcessInstance processInstance, Long FormId) {
        if (processInstance == null || processInstance.getHistory() == null || processInstance.getHistory().isEmpty()) {
            throw new IllegalArgumentException("Invalid process instance or history is empty.");
        }

        var firstHistory = processInstance.getHistory().getFirst();
        var lastHistory = processInstance.getHistory().getLast();
        String rejectComment = Optional.ofNullable(lastHistory.getComments()).orElse("");

        return ProcessInstanceDTO.builder()
                .requesterName(keycloakSecurityUtil.getUserNameById(processInstance.getActorId()))
                .processName(processInstance.getProcessDefinition().getName())
                .id(processInstance.getId())
                .formData(processInstance.getFormData())
                .createdAt(firstHistory.getTimestamp())
                .rejectionComment(rejectComment)
                .FormId(FormId)
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
                .description(formSchema.getDescription())
                .name(formSchema.getName())
                .lastUpdate(formSchema.getLastUpdate())
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

