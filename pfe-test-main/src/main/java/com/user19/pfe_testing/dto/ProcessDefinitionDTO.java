package com.user19.pfe_testing.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessDefinitionDTO {
    private Long id;
    private String name;
    private List<ProcessStepDTO> steps;

}

