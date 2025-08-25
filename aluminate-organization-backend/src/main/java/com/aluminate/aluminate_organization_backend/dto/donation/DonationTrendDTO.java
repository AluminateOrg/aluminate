package com.aluminate.aluminate_organization_backend.dto.donation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DonationTrendDTO {
    private String period;
    private long donationCount;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private long uniqueDonors;
}