package com.aluminate.aluminate_organization_backend.dto.campaign;

import com.aluminate.aluminate_organization_backend.model.CampaignType;
import lombok.Builder;
import lombok.Data;

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
    private BigDecimal goal;
    private BigDecimal raised;
    private LocalDate startDate;
    private LocalDate endDate;
    private int donorCount;
    private boolean isActive;
    private boolean isDeleted;
    private LocalDateTime deletedAt;
}