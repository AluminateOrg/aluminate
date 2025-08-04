package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
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
 * Controller for managing campaign-related operations for admin users.
 * Provides endpoints for creating, retrieving, updating, and deleting campaigns.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/campaign")
public class CampaignController {
    private final ICampaignService campaignService;

    /**
     * Creates a new campaign.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createCampaign(@RequestBody CreateCampaignRequest request) {
        try {
            CampaignResponseDTO createdCampaign = campaignService.createCampaign(request);
            return ResponseEntity.status(CREATED)
                    .body(new ApiResponse("Campaign created successfully", createdCampaign));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to create campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all active campaigns.
     */
    @GetMapping("/get/all")
    public ResponseEntity<ApiResponse> getAllCampaigns() {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getAllCampaigns();
            return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a campaign by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCampaignById(@PathVariable Long id) {
        try {
            CampaignResponseDTO campaign = campaignService.getCampaignById(id);
            return ResponseEntity.ok(new ApiResponse("Campaign retrieved successfully", campaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Updates the active status of a campaign.
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleCampaignStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        try {
            CampaignResponseDTO updatedCampaign = campaignService.updateCampaignStatus(id, isActive);
            String message = isActive ? "Campaign activated successfully" : "Campaign deactivated successfully";
            return ResponseEntity.ok(new ApiResponse(message, updatedCampaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update campaign status: " + e.getMessage(), null));
        }
    }

    /**
     * Updates an existing campaign.
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateCampaign(
            @PathVariable Long id,
            @RequestBody UpdateCampaignRequest request) {
        try {
            CampaignResponseDTO updatedCampaign = campaignService.updateCampaign(id, request);
            return ResponseEntity.ok(new ApiResponse("Campaign updated successfully", updatedCampaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Deletes a campaign by its ID (soft delete).
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteCampaign(@PathVariable Long id) {
        try {
            CampaignResponseDTO deletedCampaign = campaignService.deleteCampaign(id);
            return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", deletedCampaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to delete campaign: " + e.getMessage(), null));
        }
    }
}