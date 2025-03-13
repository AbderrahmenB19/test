package com.user19.pfe_testing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue("NOTIFY")
public class NotificationStep extends ProcessStep {
    @ElementCollection
    @Column(columnDefinition = "LONGTEXT")
    private List<String> recipients;

    @Column(columnDefinition = "LONGTEXT")
    private String message;
}
