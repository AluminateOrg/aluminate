package com.aluminate.aluminate_organization_backend.chat.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.aluminate.aluminate_organization_backend.chat.service.ChatService.TooManyRequestsException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandlerChat {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex, req);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> forbidden(SecurityException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex, req);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> tooMany(TooManyRequestsException ex, HttpServletRequest req) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, new IllegalArgumentException(ex.getMessage()), req);
    }

    @ExceptionHandler(MessageDeliveryException.class)
    public ResponseEntity<ErrorResponse> ws(MessageDeliveryException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, new IllegalArgumentException(ex.getMessage()), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unknown(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex, req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
