package com.aluminate.aluminate_organization_backend.dto.donation;

import com.aluminate.aluminate_organization_backend.model.DonationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DonationResponseDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long campaignId;
    private String campaignTitle;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDateTime createdAt;
    private String paymentOrderId;
    private DonationStatus status;
    private boolean isAnonymous;
    private String paymentMethod;
    private String transactionId;
}
