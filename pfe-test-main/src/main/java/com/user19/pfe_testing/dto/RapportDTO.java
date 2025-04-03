package com.user19.pfe_testing.dto;

import com.user19.pfe_testing.model.enums.ProcessStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RapportDTO {
    private Long processInstanceId;
    private String processName;
    private LocalDateTime startTime;
    private List<ProcessHistoryDTO> processHistoryDTOList;
    private ProcessStatus currentStatus;

    public static class RapportDTOBuilder {
        private Long processInstanceId;
        private String processName;
        private LocalDateTime startTime;
        private List<ProcessHistoryDTO> processHistoryDTOList;
        private ProcessStatus currentStatus;

        public RapportDTOBuilder processInstanceId(Long processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public RapportDTOBuilder processName(String processName) {
            this.processName = processName;
            return this;
        }

        public RapportDTOBuilder processHistoryDTOList(List<ProcessHistoryDTO> processHistoryDTOList) {
            this.processHistoryDTOList = processHistoryDTOList;
            return this;
        }

        public RapportDTO build() {
            if (processHistoryDTOList != null && !processHistoryDTOList.isEmpty()) {
                this.startTime = processHistoryDTOList.get(0).getTimestamp();
                this.currentStatus = processHistoryDTOList.stream()
                        .filter(e -> e.getActionStatus() != null)
                        .reduce((first, second) -> second)
                        .orElseThrow(() -> new RuntimeException("No valid process history found"))
                        .getActionStatus();
            }
            return new RapportDTO(processInstanceId, processName, startTime, processHistoryDTOList, currentStatus);
        }
    }
}
