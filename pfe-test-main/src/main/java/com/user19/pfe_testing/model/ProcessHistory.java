package com.user19.pfe_testing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ProcessHistory {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    private String action;
    private String comments;
    private ProcessStatus actionStatus;     //TODO for other classes to update process status
    
    @Column(name = "timestamp", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @ManyToOne
    private ProcessInstance processInstance;

    private String actorId;
}