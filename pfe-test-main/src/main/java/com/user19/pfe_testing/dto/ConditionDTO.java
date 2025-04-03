package com.user19.pfe_testing.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionDTO {
    String condition;
    String targetStep;
}
