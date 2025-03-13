package com.user19.pfe_testing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProcessInstanceDTO {
    private String processInstanceId;
    private String formData;
}
