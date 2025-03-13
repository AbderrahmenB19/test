package com.user19.pfe_testing.dto;


import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessDefinitionDTO {
    private String id;
    private String name;
    private FormSchemaDTO formSchema;
    private List<ProcessStepDTO> steps;

}

