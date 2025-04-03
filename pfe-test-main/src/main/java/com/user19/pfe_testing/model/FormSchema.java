package com.user19.pfe_testing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "form_schemas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormSchema {

    @Id
    @GeneratedValue

    private Long id;

    @Column(columnDefinition = "TEXT")
    private String jsonSchema;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "process_definition_id", referencedColumnName = "id", unique = true)
    @JsonIgnore
    private ProcessDefinition processDefinition;
}