package com.aluminate.aluminate_organization_backend.chat.exception;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
}
