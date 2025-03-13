package com.user19.pfe_testing.dto;



import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class ApprovalStepDTO extends ProcessStepDTO {
    private List<String> validatorRoles;
    private Integer numberOfValidators;
    private String requiredApproval;

    public ApprovalStepDTO(String name, int stepOrder, List<String> validatorRoles, Integer numberOfValidators, String requiredApproval) {
        super(name,  "APPROVAL");
        this.validatorRoles = validatorRoles;
        this.numberOfValidators = numberOfValidators;
        this.requiredApproval = requiredApproval;
    }




}
