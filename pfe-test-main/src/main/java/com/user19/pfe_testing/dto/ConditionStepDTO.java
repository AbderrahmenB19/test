package com.user19.pfe_testing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConditionStepDTO extends ProcessStepDTO {

    public ConditionStepDTO(String name, int stepOrder, String conditionExpression, String onTrueStepId, String onFalseStepId) {
        super(name,  "CONDITION");
        this.conditionExpression = conditionExpression;
        this.onTrueStepId = onTrueStepId;
        this.onFalseStepId = onFalseStepId;
    }

    private String conditionExpression;
    private String onTrueStepId;
    private String onFalseStepId;


}