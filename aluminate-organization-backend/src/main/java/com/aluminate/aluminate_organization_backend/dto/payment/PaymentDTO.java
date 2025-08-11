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
}
