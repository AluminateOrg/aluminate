package com.aluminate.aluminate_organization_backend.dto.campaign;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CampaignStatsDTO {
    private long totalCampaigns;
    private long activeCampaigns;
    private long inactiveCampaigns;
    private long expiredCampaigns;
    private long deletedCampaigns;
    private BigDecimal totalGoal;
    private BigDecimal totalRaised;
    private int totalDonors;
    private BigDecimal averageProgress;
    private BigDecimal averageDonationAmount;
    private String mostPopularCampaignType;
    private BigDecimal highestGoal;
    private BigDecimal highestRaised;
    private long campaignsNeedingAttention;
}