package com.aluminate.aluminate_organization_backend.config.exception;

import jakarta.validation.constraints.NotBlank;

public class EmailNotVerifiedException extends Throwable {
    public EmailNotVerifiedException(@NotBlank(message = "Email is not Verified") String s) {
    }
}
