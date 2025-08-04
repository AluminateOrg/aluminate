package com.aluminate.aluminate_organization_backend.service.campaign;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;

import java.util.List;

/**
 * Service interface for managing campaign operations.
 */
public interface ICampaignService {
    /**
     * Creates a new campaign based on the provided request.
     */
    CampaignResponseDTO createCampaign(CreateCampaignRequest request);

    /**
     * Retrieves all active campaigns.
     */
    List<CampaignResponseDTO> getAllCampaigns();

    /**
     * Retrieves a campaign by its ID.
     */
    CampaignResponseDTO getCampaignById(Long id);

    /**
     * Updates the active status of a campaign.
     */
    CampaignResponseDTO updateCampaignStatus(Long id, boolean isActive);

    /**
     * Deletes a campaign by its ID (soft delete).
     */
    CampaignResponseDTO deleteCampaign(Long id);
    CampaignResponseDTO updateCampaign(Long id, UpdateCampaignRequest request);
}