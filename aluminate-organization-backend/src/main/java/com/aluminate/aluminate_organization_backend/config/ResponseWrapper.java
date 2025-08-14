package com.aluminate.aluminate_organization_backend.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResponseWrapper <T> {
    private boolean success;
    private String message;
    private T data;

    public ResponseWrapper(boolean success, String message) {
        this(success, message, null);
    }

    public ResponseWrapper(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
