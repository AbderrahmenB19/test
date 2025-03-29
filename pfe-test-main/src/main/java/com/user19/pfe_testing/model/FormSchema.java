package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Column(columnDefinition = "LONGTEXT")
    private String jsonSchema;

    @OneToOne(mappedBy = "formSchema", cascade = CascadeType.ALL)
    private ProcessDefinition processDefinition;
}
