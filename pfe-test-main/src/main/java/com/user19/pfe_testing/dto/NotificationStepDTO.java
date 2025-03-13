package com.user19.pfe_testing.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class NotificationStepDTO extends ProcessStepDTO {

    public NotificationStepDTO(String name, List<String> recipients, String message) {
        super(name, "NOTIFY");
        this.recipients = recipients;
        this.message = message;
    }

    private List<String> recipients;
    private String message;
}