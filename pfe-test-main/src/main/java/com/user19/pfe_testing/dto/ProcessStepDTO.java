package com.user19.pfe_testing.dto;

import com.user19.pfe_testing.model.Condition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class ProcessStepDTO {
    private String name;
    private String stepType;
    private List<String> validatorRoles;
    private String requiredApproval;
    private List<Condition> condition;
    private List<String> recipients;
    private String message;


    public ProcessStepDTO(String name, String stepType) {
        this.name = name;
        this.stepType = stepType;
    }
}

