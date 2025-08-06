package com.aluminate.aluminate_organization_backend.dto.campaign;

import com.aluminate.aluminate_organization_backend.model.CampaignType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCampaignRequest {
    private String title;
    private String description;
    private CampaignType type;
    private BigDecimal goal;
    private LocalDate endDate;
    private boolean isActive; // New field to control active status on creation
}