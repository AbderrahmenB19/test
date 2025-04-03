package com.user19.pfe_testing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @OneToOne(mappedBy = "processDefinition", cascade = CascadeType.ALL)

    private FormSchema formSchema;

    @OneToMany(mappedBy = "processDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessStep> steps= new ArrayList<>() ;
}
