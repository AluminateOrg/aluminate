package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum EventStatus {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

}
