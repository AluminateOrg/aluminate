package com.aluminate.aluminate_organization_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MemberRowDTO {
    @NotBlank(message = "NIC is required")
    @Pattern(regexp = "^(\\d{9}[Vv]|\\d{12})$", message = "Invalid NIC format")
    private String nic;

    @NotBlank(message = "Name is required")
    private  String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+94\\d{9}$", message = "Phone must start with +94 and have 12 digits")
    private String phone;

    @NotBlank(message = "Registration number is required")
    private String regNo;

    @NotNull(message = "Batch is required")
    private Integer batch;

    private String status = "valid";
    private Map<String, String> errors = new HashMap<>();
    private Map<String, String> suggestions = new HashMap<>();

}
