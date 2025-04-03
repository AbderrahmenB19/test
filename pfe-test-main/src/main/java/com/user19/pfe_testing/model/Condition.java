package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)

    private Long conditionId;

    private String condition;
    private String targetStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_step_id")
    private ConditionStep conditionStep;
}
