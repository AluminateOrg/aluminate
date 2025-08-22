package com.aluminate.aluminate_organization_backend.config;

import com.aluminate.aluminate_organization_backend.config.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle custom exceptions (e.g., AlreadyExistsException)



    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseWrapper<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateOrganizationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDuplicateOrg(DuplicateOrganizationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseWrapper<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, ex.getMessage(), null));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ResponseWrapper<>(false, "Invalid request format: " + ex.getLocalizedMessage(), null));
    }

    @ExceptionHandler(InactiveOrganizationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInactiveOrganization(InactiveOrganizationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseWrapper<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleInvalidEmail(InvalidEmailException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseWrapper<>(false, ex.getMessage(), null));
    }

    // Keep generic handler as fallback
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains(":")) {
            message = message.substring(message.indexOf(":") + 1).trim();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseWrapper<>(false, message, null));
    }

    // Handle validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ResponseWrapper<>(false, errorMessages, null),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                new ResponseWrapper<>(false, ex.getMessage(), null),
                HttpStatus.BAD_REQUEST
        );
    }

    // Catch-all for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleAllExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
                new ResponseWrapper<>(false, "Internal server error", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


}
