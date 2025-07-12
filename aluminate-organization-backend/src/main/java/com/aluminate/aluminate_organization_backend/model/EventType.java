package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum EventType {
    SOCIAL("Social Event"),
    WORKSHOP("Workshop"),
    OTHER("Other Event");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }
}
