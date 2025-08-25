package com.aluminate.aluminate_organization_backend.dto.donation;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationRequestDTO {

    @NotNull(message = "Campaign ID is required")
    @Positive(message = "Campaign ID must be positive")
    private Long campaignId;

    @NotNull(message = "Member ID is required")
    @Positive(message = "Member ID must be positive")
    private Long memberId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @DecimalMax(value = "1000000.00", message = "Amount cannot exceed 1,000,000.00")
    private BigDecimal amount;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+94|0)[0-9]{9}$", message = "Phone number must be a valid Sri Lankan number")
    private String phone;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @Size(max = 50, message = "Country cannot exceed 50 characters")
    private String country;

    private boolean isAnonymous;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

    @Size(max = 20, message = "Payment method cannot exceed 20 characters")
    private String paymentMethod;
}