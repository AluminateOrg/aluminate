package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayHerePaymentResponse {
    private String orderId;
    private String hash;
    private String merchantId;
    private String amount;
    private String currency;
    private String itemDescription;
    private boolean sandbox;
}