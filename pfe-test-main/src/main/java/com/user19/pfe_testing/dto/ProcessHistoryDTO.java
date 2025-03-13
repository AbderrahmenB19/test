package com.user19.pfe_testing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String comments;
    private LocalDateTime timestamp;

}
