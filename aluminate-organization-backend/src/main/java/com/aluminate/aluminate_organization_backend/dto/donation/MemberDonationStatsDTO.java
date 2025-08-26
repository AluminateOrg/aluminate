package com.aluminate.aluminate_organization_backend.dto.donation;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MemberDonationStatsDTO {
    private Long memberId;
    private String memberName;
    private BigDecimal totalDonated;
    private long totalDonations;
    private long completedDonations;
    private long pendingDonations;
    private BigDecimal averageDonation;
    private long campaignsSupported;
}