package com.aluminate.aluminate_organization_backend.model;

import lombok.Getter;

@Getter
public enum GroupCategory {
    PROFESSIONAL("Professional group"),
    SOCIAL("Social group"),
    HOBBY("Hobby group"),
    ACADEMIC("Academic group");

    private final String description;

    GroupCategory(String description) {
        this.description = description;
    }
}
