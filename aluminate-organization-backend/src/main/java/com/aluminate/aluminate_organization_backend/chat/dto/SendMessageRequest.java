package com.aluminate.aluminate_organization_backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank
    @Size(max = 8192)
    private String content;
}
