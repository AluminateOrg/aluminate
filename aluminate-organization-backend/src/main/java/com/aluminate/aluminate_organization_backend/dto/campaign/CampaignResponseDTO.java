package com.aluminate.aluminate_organization_backend.dto.campaign;

import com.aluminate.aluminate_organization_backend.model.CampaignType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder

public class CampaignResponseDTO {
    private Long id;
    private String title;
    private String description;
    private CampaignType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal goal;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal raised;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private int donorCount;
    private boolean isActive;
    private boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Computed fields
    private Double progressPercentage;
    private boolean isExpired;
    private boolean canAcceptDonations;
    private Long daysRemaining;
    private String status;
    private BigDecimal remainingAmount;
    private boolean isGoalAchieved;
}