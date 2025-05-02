package com.user19.pfe_testing.dto;


import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class FormSchemaDTO{
    private Long id;
    private String jsonSchema;
    private String name;
    private String description;
    private LocalDateTime lastUpdate;


}

