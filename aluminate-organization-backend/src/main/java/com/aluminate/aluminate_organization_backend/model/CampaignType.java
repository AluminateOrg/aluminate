package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum CampaignType {
    FUNDRAISING("Fundraising Campaign"),
    EMERGENCY("Emergency Relief"),
    INFRASTRUCTURE("Infrastructure Development"),
    OTHER("Other Campaign");

    private final String displayName;

    CampaignType(String displayName) {
        this.displayName = displayName;
    }
}
