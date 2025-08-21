package com.aluminate.aluminate_organization_backend.dto.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class PaymentDTO {
    private Long id;
    private String status;
    private Double amount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private Long payerId;

    // For PayHere specific fields
    private String orderId;
    private String hash;

    public PaymentDTO() {}

    public PaymentDTO(Long id, String status, Double amount, LocalDateTime paymentDate,
                      String paymentMethod, Long payerId, String orderId, String hash) {
        this.id = id;
        this.status = status;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.payerId = payerId;
        this.orderId = orderId;
        this.hash = hash;
    }

    // Constructor for PayHere response
    public PaymentDTO(String orderId, String hash, String amount) {
        this.orderId = orderId;
        this.hash = hash;
        this.amount = Double.parseDouble(amount);
    }

    public void setAmount(String amount) {
        this.amount = Double.parseDouble(amount);
    }
}