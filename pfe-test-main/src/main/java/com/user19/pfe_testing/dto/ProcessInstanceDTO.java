package com.user19.pfe_testing.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessInstanceDTO {
    private Long id;
    private String status;
    private Long FormId;
    private String processName;
    private String requesterName;
    private LocalDateTime createdAt;
    private LocalDateTime decisionDate;
    private String formData;
    private String rejectionComment;
}
