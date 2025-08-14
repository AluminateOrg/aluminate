package com.aluminate.aluminate_organization_backend.config.exception;

public class DuplicateOrganizationException extends RuntimeException {
    public DuplicateOrganizationException(String message) {
        super(message);
    }
}
