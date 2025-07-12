package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum CardType {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express");

    private final String displayName;

    CardType(String displayName) {
        this.displayName = displayName;
    }
}
