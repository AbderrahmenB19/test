package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@DiscriminatorValue("APPROVAL")
public class ApprovalStep extends ProcessStep {
    @ElementCollection
    @CollectionTable(name = "approval_step_roles", joinColumns = @JoinColumn(name = "approval_step_id" ))
    @Column(name = "role" )
    private List<String> validatorRoles;

    @Column(length = 500)
    private String requiredApproval;
}