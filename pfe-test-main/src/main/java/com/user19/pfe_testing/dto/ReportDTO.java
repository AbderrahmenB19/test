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
public class ReportDTO {
    private String username;
    private Long processInstanceId;
    private String processName;
    private LocalDateTime startTime;
    private List<ProcessHistoryDTO> processHistoryDTOList;
    private ProcessStatus currentStatus;
    private String processDefinitionName;

    public static class ReportDTOBuilder {
        private String username;
        private Long processInstanceId;
        private String processName;
        private LocalDateTime startTime;
        private List<ProcessHistoryDTO> processHistoryDTOList;
        private ProcessStatus currentStatus;
        private String processDefinitionName;

        public ReportDTOBuilder processInstanceId(Long processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public ReportDTOBuilder processName(String processName) {
            this.processName = processName;
            return this;
        }
        public ReportDTOBuilder processDefinitionName(String processDefinitionName) {
            this.processDefinitionName = processDefinitionName;
            return this;
        }

        public ReportDTOBuilder processHistoryDTOList(List<ProcessHistoryDTO> processHistoryDTOList) {
            this.processHistoryDTOList = processHistoryDTOList;
            return this;
        }
        public ReportDTOBuilder currentStatus(ProcessStatus currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }
        public ReportDTOBuilder username(String username) {
            this.username = username;
            return this;
        }


        public ReportDTO build() {
            if (processHistoryDTOList != null && !processHistoryDTOList.isEmpty()) {
                this.startTime = processHistoryDTOList.get(0).getTimestamp();


            }
            return new ReportDTO(username,processInstanceId, processName, startTime, processHistoryDTOList, currentStatus, processDefinitionName);
        }
    }
}
