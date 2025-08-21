package com.aluminate.aluminate_organization_backend.model;

public enum GroupJoinRequestStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String displayName;

    GroupJoinRequestStatus(String displayName) {
        this.displayName = displayName;
    }
}
