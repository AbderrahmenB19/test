package com.user19.pfe_testing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessHistoryDTO {

    private String action;
    private ProcessStatus actionStatus;
    private String comments; // in case there is reject
    private LocalDateTime timestamp;

}
