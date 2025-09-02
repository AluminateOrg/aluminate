package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum NotificationType {
    EVENT("Event"),
    GROUP("Group"),
    DONATION("Donation"),
    SYSTEM("System"),
    INFO("Info"),
    MENTORSHIP("Mentorship"),
    WARNING("Warning"),
    SUCCESS("Success");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }
}