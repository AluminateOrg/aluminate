package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayHereNotifyRequest {
    private String merchant_id;
    private String order_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String custom_1; // We'll use this for donation ID
    private String custom_2; // We'll use this for campaign ID
    private String method;
    private String status_message;
    private String card_holder_name;
    private String card_no;
    private String card_expiry;
}