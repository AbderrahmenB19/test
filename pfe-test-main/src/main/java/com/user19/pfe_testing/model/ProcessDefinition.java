package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessDefinition {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    private String name;

    @OneToOne
    @JoinColumn(name = "form_schema_id", referencedColumnName = "id", unique = true)
    private FormSchema formSchema;

    @OneToMany(mappedBy = "processDefinition", cascade = CascadeType.ALL)
    private List<ProcessStep> steps = new ArrayList<>();
}
