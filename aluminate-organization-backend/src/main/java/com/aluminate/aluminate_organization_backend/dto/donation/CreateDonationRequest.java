package com.aluminate.aluminate_organization_backend.dto.donation;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDonationRequest {
    private Long campaignId;
    private BigDecimal amount;
    private boolean isAnonymous;
    private String paymentMethod; // Default to "PAYHERE"
}