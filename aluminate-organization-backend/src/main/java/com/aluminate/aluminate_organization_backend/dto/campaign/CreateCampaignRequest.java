package com.aluminate.aluminate_organization_backend.dto.campaign;

import com.aluminate.aluminate_organization_backend.model.CampaignType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCampaignRequest {

    @NotBlank(message = "Campaign title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Campaign type is required")
    private CampaignType type;

    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "100.0", message = "Goal must be at least LKR 100")
    @DecimalMax(value = "10000000.0", message = "Goal cannot exceed LKR 10,000,000")
    private BigDecimal goal;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private boolean isActive = true;
}