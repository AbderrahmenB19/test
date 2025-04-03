package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("CONDITION")
@SuperBuilder
public class ConditionStep extends ProcessStep {
    @OneToMany(mappedBy = "conditionStep", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Condition> conditions = new ArrayList<>();

}