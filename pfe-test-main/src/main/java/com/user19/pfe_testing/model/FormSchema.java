package com.user19.pfe_testing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
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
    private String name;
    private String description;
    private LocalDateTime lastUpdate;



}