package com.aluminate.aluminate_organization_backend.dto.donation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DonationStatsDTO {
    private BigDecimal totalDonated;
    private long totalDonations;
    private long completedDonations;
    private long pendingDonations;
    private long failedDonations;
    private long uniqueDonors;
    private long campaignsWithDonations;
    private BigDecimal averageDonation;
    private BigDecimal highestDonation;
    private BigDecimal lowestDonation;
}