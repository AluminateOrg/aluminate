package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    CANCELLED("Cancelled");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}