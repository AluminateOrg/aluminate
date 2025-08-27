package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.service.campaign.ICampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * Controller for member access to campaigns for donation purposes.
 * Separate from admin campaign management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/member/campaign")
@CrossOrigin(origins = "*")
public class MemberCampaignController {

    private final ICampaignService campaignService;

    /**
     * Retrieves all active campaigns that members can donate to.
     */
    @GetMapping("/get/active")
    public ResponseEntity<ApiResponse> getActiveCampaigns() {
        try {
            log.info("Fetching active campaigns for member donation");
            List<CampaignResponseDTO> campaigns = campaignService.getActiveCampaignsForDonation();
            log.info("Retrieved {} active campaigns", campaigns.size());
            return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            log.error("Error retrieving active campaigns", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all active campaigns with pagination and search for donation purposes.
     */
    @GetMapping("/active/paginated")
    public ResponseEntity<ApiResponse> getActiveCampaignsPaginated(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        try {
            log.info("Fetching paginated active campaigns, search: {}", search);

            if (search != null && !search.trim().isEmpty()) {
                Page<CampaignResponseDTO> campaigns = campaignService.searchCampaigns(search.trim(), pageable);
                // Filter only active campaigns that can accept donations
                Page<CampaignResponseDTO> activeCampaigns = campaigns.map(campaign -> {
                    if (campaign.isCanAcceptDonations()) {
                        return campaign;
                    }
                    return null;
                }).map(campaign -> campaign);

                return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", activeCampaigns));
            } else {
                List<CampaignResponseDTO> allActiveCampaigns = campaignService.getActiveCampaignsForDonation();
                return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", allActiveCampaigns));
            }
        } catch (Exception e) {
            log.error("Error retrieving paginated active campaigns", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a specific active campaign by ID for donation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getActiveCampaignById(@PathVariable Long id) {
        try {
            log.info("Fetching active campaign by ID: {}", id);
            CampaignResponseDTO campaign = campaignService.getActiveCampaignById(id);
            log.info("Retrieved campaign: {}", campaign.getTitle());
            return ResponseEntity.ok(new ApiResponse("Campaign retrieved successfully", campaign));
        } catch (ResourceNotFoundException e) {
            log.warn("Campaign not found with ID: {}", id);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error retrieving campaign with ID: {}", id, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Get campaign statistics that are relevant to members
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getPublicCampaignStats() {
        try {
            log.info("Fetching public campaign statistics");

            // Get only public-facing statistics
            List<CampaignResponseDTO> activeCampaigns = campaignService.getActiveCampaignsForDonation();

            // Calculate basic stats for members
            int totalActiveCampaigns = activeCampaigns.size();
            long totalRaised = activeCampaigns.stream()
                    .mapToLong(campaign -> campaign.getRaised().longValue())
                    .sum();
            long totalGoal = activeCampaigns.stream()
                    .mapToLong(campaign -> campaign.getGoal().longValue())
                    .sum();
            int totalDonors = activeCampaigns.stream()
                    .mapToInt(CampaignResponseDTO::getDonorCount)
                    .sum();

            var publicStats = java.util.Map.of(
                    "activeCampaigns", totalActiveCampaigns,
                    "totalRaised", totalRaised,
                    "totalGoal", totalGoal,
                    "totalDonors", totalDonors,
                    "progressPercentage", totalGoal > 0 ? (double) totalRaised / totalGoal * 100 : 0
            );

            return ResponseEntity.ok(new ApiResponse("Public campaign statistics retrieved successfully", publicStats));
        } catch (Exception e) {
            log.error("Error retrieving public campaign statistics", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve statistics: " + e.getMessage(), null));
        }
    }
}