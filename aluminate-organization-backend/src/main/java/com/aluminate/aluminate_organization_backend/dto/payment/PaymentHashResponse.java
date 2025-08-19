package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHashResponse {
    private String orderId;
    private String hash;
    private String amount;
    private String merchantId;
    private String currency;
}
