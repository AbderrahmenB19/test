package com.user19.pfe_testing.dto;

import com.user19.pfe_testing.model.ProcessHistory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RapportDTO {
    String processInstanceId;
    List<ProcessHistoryDTO> processHistoryDTOList;
}
