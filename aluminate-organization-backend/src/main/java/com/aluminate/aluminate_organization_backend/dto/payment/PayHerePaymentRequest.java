package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayHerePaymentRequest {
    private Long campaignId;
    private Long memberId;
    private BigDecimal amount;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private boolean isAnonymous;
    private String message;
}