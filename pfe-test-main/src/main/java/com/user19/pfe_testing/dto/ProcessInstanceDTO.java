package com.user19.pfe_testing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProcessInstanceDTO {
    private String id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime decisionDate;
    private String formData;
    private String rejectionComment;
}
