package com.aluminate.aluminate_organization_backend.service.campaign;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignStatsDTO;
import com.aluminate.aluminate_organization_backend.model.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICampaignService {

    /**
     * Creates a new campaign based on the provided request.
     */
    CampaignResponseDTO createCampaign(CreateCampaignRequest request);

    /**
     * Updates an existing campaign.
     */
    CampaignResponseDTO updateCampaign(Long id, UpdateCampaignRequest request);

    /**
     * Retrieves all campaigns (excluding deleted ones).
     */
    List<CampaignResponseDTO> getAllCampaigns();

    /**
     * Retrieves all active campaigns for donation purposes.
     */
    List<CampaignResponseDTO> getActiveCampaignsForDonation();

    /**
     * Retrieves a campaign by its ID.
     */
    CampaignResponseDTO getCampaignById(Long id);

    /**
     * Retrieves an active campaign by ID for donation.
     */
    CampaignResponseDTO getActiveCampaignById(Long id);

    /**
     * Updates the active status of a campaign.
     */
    CampaignResponseDTO updateCampaignStatus(Long id, boolean isActive);

    /**
     * Deletes a campaign by its ID (soft delete).
     */
    CampaignResponseDTO deleteCampaign(Long id);

    /**
     * Searches campaigns by title or description.
     */
    Page<CampaignResponseDTO> searchCampaigns(String searchTerm, Pageable pageable);

    /**
     * Retrieves campaigns by type.
     */
    List<CampaignResponseDTO> getCampaignsByType(CampaignType type);

    /**
     * Retrieves comprehensive campaign statistics.
     */
    CampaignStatsDTO getCampaignStatistics();

    /**
     * Retrieves recent campaigns.
     */
    List<CampaignResponseDTO> getRecentCampaigns(int limit);

    /**
     * Retrieves campaigns nearing their deadline.
     */
    List<CampaignResponseDTO> getCampaignsNearingDeadline(int days);
}