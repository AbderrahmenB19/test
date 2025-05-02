package com.user19.pfe_testing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.user19.pfe_testing.model.Condition;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessStepDTO {
    private Long id;
    private String name;
    private String stepType;//NOTIFY,APPROVAL,CONDITION
    private Long formId;
    private List<String> validatorRoles;
    private String requiredApproval; //ANY,ALL,number for validator need
    private List<ConditionDTO> condition;
    private List<String> recipients; //List of email if step NOTIFY
    private String message;



}

