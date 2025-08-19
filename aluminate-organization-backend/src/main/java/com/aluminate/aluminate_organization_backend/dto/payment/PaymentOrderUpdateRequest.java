package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.Data;

/**
 * DTO for updating donation with PayHere payment order ID
 */
@Data
public class PaymentOrderUpdateRequest {
    private String paymentOrderId;
}