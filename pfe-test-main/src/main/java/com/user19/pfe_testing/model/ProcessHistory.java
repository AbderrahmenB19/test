package com.user19.pfe_testing.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "process_history")
public class ProcessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(updatable = false)
    private Long id;

    private String action;

    private String comments;

    @Enumerated(EnumType.STRING)
    private ProcessStatus actionStatus;

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @ManyToOne
    private ProcessInstance processInstance;

    private String actorId;

    @PrePersist
    protected void setTimestamp() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}