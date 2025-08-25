package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum CampaignType {
    FUNDRAISING("Fundraising"),
    EMERGENCY("Emergency"),
    GENERAL("General"),
    SCHOLARSHIP("Scholarship"),
    INFRASTRUCTURE("Infrastructure"),
    OTHER("Other");

    private final String displayName;

    CampaignType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}