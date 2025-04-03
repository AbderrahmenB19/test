package com.user19.pfe_testing.model;

import com.user19.pfe_testing.model.enums.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity

public class ProcessInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String currentStepName;

    private String formData;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @ManyToOne
    private ProcessDefinition processDefinition;

    @OneToMany(mappedBy = "processInstance", cascade = CascadeType.ALL)
    private List<ProcessHistory> history ;

    private String actorId;
}