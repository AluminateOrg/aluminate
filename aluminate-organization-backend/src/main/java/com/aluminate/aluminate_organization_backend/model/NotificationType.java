package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum NotificationType {
    EVENT("Event notification"),
    GROUP("Group notification"),
    DONATION("Donation notification"),
    SYSTEM("System notification"),
    INFO("Information"),
    MENTORSHIP("Mentorship notification"),
    WARNING("Warning"),
    SUCCESS("Success");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }
}
