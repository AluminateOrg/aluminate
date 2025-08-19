package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.service.campaign.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * Controller for member access to campaigns for donation purposes.
 * Separate from admin campaign management.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/member/campaign")
public class MemberCampaignController {

    private final ICampaignService campaignService;

    /**
     * Retrieves all active campaigns that members can donate to.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveCampaigns() {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getActiveCampaignsForDonation();
            return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
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
            CampaignResponseDTO campaign = campaignService.getActiveCampaignById(id);
            return ResponseEntity.ok(new ApiResponse("Campaign retrieved successfully", campaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign: " + e.getMessage(), null));
        }
    }
}