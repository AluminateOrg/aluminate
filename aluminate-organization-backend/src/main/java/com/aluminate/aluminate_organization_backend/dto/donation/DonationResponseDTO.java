package com.aluminate.aluminate_organization_backend.dto.donation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DonationResponseDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private Long campaignId;
    private String campaignTitle;
    private Long memberId;
    private String memberName;
    private boolean isAnonymous;
    private String message;
    private String paymentMethod;
    private String status;
    private String paymentStatus;
    private String transactionId;
    private String paymentOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}