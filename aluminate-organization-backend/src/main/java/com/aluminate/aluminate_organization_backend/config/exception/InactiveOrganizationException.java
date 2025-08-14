package com.aluminate.aluminate_organization_backend.config.exception;

public class InactiveOrganizationException extends Throwable {
    public InactiveOrganizationException(String organizationIsNotActive) {
        super(organizationIsNotActive);
    }
}
