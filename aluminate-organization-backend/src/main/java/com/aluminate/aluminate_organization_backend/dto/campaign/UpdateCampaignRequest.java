package com.aluminate.aluminate_organization_backend.dto.campaign;

import com.aluminate.aluminate_organization_backend.model.CampaignType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateCampaignRequest {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private CampaignType type;

    @DecimalMin(value = "100.0", message = "Goal must be at least LKR 100")
    @DecimalMax(value = "10000000.0", message = "Goal cannot exceed LKR 10,000,000")
    private BigDecimal goal;

    private LocalDate endDate;
}