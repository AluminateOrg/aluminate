package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentHashRequest {
    private BigDecimal amount;
    private Long campaignId;
    private Long memberId;
    private boolean isAnonymous;
}
