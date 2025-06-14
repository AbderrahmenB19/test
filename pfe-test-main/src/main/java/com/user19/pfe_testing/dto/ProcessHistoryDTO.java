package com.user19.pfe_testing.dto;

import com.user19.pfe_testing.model.enums.ProcessStatus;
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
    private String comments;
    private LocalDateTime timestamp;

}
