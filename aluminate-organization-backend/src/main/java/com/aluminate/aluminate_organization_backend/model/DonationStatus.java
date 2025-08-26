package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum DonationStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    DonationStatus(String displayName) {
        this.displayName = displayName;
    }
}