package com.user19.pfe_testing.dto;

import com.user19.pfe_testing.model.ProcessHistory;
import com.user19.pfe_testing.model.enums.ProcessStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RapportDTO {
    //TODO update usage
    private String processInstanceId;
    private String processName;
    private LocalDateTime startTime;
    private List<ProcessHistoryDTO> processHistoryDTOList;
    private ProcessStatus currentStatus;
    RapportDTO(List<ProcessHistoryDTO> processHistoryDTOList) {
        this.startTime=processHistoryDTOList.getFirst().getTimestamp();
        this.currentStatus= processHistoryDTOList.stream().filter(e->e.getActionStatus()!=null).toList().getLast().getActionStatus();

    }
}
