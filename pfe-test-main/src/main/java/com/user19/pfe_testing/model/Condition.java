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
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String conditionId;
    private String condition;
    private String targetStep;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_step_id")
    private ConditionStep conditionStep;

}
