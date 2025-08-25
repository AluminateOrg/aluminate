package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotifyRequest {

    private String merchant_id;
    private String order_id;
    private String payment_id;
    private String payhere_amount;
    private String payhere_currency;
    private String status_code;
    private String md5sig;
    private String custom_1;
    private String custom_2; //email
    private String method;
    private String status_message;

    // Optional fields (only if paid with VISA/MASTER)
    private String card_holder_name;
    private String card_no;
    private String card_expiry;
}
