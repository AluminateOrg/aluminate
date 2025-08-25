package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignStatsDTO;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.CampaignType;
import com.aluminate.aluminate_organization_backend.service.campaign.ICampaignService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * Controller for managing campaign-related operations.
 * Provides endpoints for creating, retrieving, updating, and deleting campaigns.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/admin/campaign")
@Validated
@CrossOrigin(origins = "*")
public class CampaignController {
    private final ICampaignService campaignService;

    /**
     * Creates a new campaign with validation.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        try {
            log.info("Creating new campaign with title: {}", request.getTitle());
            log.info("Request payload: {}", request);

            CampaignResponseDTO createdCampaign = campaignService.createCampaign(request);
            log.info("Campaign created successfully with ID: {}", createdCampaign.getId());

            return ResponseEntity.status(CREATED)
                    .body(new ApiResponse("Campaign created successfully", createdCampaign));
        } catch (IllegalArgumentException e) {
            log.warn("Campaign creation failed due to validation: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during campaign creation: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to create campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all campaigns with optional search and pagination.
     */
    @GetMapping("/get/all")
    public ResponseEntity<ApiResponse> getAllCampaigns(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search) {
        try {
            log.info("Fetching all campaigns, search: {}", search);

            if (search != null && !search.trim().isEmpty()) {
                Page<CampaignResponseDTO> campaigns = campaignService.searchCampaigns(search.trim(), pageable);
                return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaigns));
            } else {
                List<CampaignResponseDTO> campaigns = campaignService.getAllCampaigns();
                log.info("Retrieved {} campaigns", campaigns.size());
                return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaigns));
            }
        } catch (Exception e) {
            log.error("Error retrieving campaigns: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves active campaigns for donation.
     */
    @GetMapping("/get/active")
    public ResponseEntity<ApiResponse> getActiveCampaignsForDonation() {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getActiveCampaignsForDonation();
            return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            log.error("Error retrieving active campaigns: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve active campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a campaign by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCampaignById(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long id) {
        try {
            CampaignResponseDTO campaign = campaignService.getCampaignById(id);
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
     * Updates an existing campaign.
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateCampaign(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long id,
            @Valid @RequestBody UpdateCampaignRequest request) {
        try {
            log.info("Updating campaign with ID: {}", id);
            CampaignResponseDTO updatedCampaign = campaignService.updateCampaign(id, request);
            log.info("Campaign updated successfully with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse("Campaign updated successfully", updatedCampaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during campaign update: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update campaign: " + e.getMessage(), null));
        }
    }

    /**
     * FIXED: Toggles campaign status (active/inactive).
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleCampaignStatus(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long id,
            @RequestParam boolean isActive) {
        try {
            log.info("=== CONTROLLER TOGGLE START ===");
            log.info("Toggling campaign {} status to: {}", id, isActive);

            CampaignResponseDTO updatedCampaign = campaignService.updateCampaignStatus(id, isActive);

            log.info("Controller received updated campaign with isActive: {}", updatedCampaign.isActive());

            String message = isActive ? "Campaign activated successfully" : "Campaign deactivated successfully";
            log.info("Returning response: {}", message);
            log.info("=== CONTROLLER TOGGLE END ===");

            return ResponseEntity.ok(new ApiResponse(message, updatedCampaign));
        } catch (ResourceNotFoundException e) {
            log.warn("Campaign not found for toggle: {}", id);
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            log.warn("Business rule violation for campaign toggle: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error toggling campaign status for ID: {}", id, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update campaign status: " + e.getMessage(), null));
        }
    }

    /**
     * Soft deletes a campaign.
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteCampaign(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long id) {
        try {
            log.info("Deleting campaign with ID: {}", id);
            CampaignResponseDTO deletedCampaign = campaignService.deleteCampaign(id);
            log.info("Campaign deleted successfully with ID: {}", id);
            return ResponseEntity.ok(new ApiResponse("Campaign deleted successfully", deletedCampaign));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting campaign with ID: {}", id, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to delete campaign: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves campaigns by type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse> getCampaignsByType(@PathVariable CampaignType type) {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getCampaignsByType(type);
            return ResponseEntity.ok(new ApiResponse("Campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            log.error("Error retrieving campaigns by type: {}", type, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns by type: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves comprehensive campaign statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getCampaignStats() {
        try {
            CampaignStatsDTO stats = campaignService.getCampaignStatistics();
            return ResponseEntity.ok(new ApiResponse("Campaign statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving campaign statistics: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve statistics: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves recent campaigns.
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentCampaigns(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be positive") int limit) {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getRecentCampaigns(limit);
            return ResponseEntity.ok(new ApiResponse("Recent campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            log.error("Error retrieving recent campaigns: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve recent campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves campaigns nearing their deadline.
     */
    @GetMapping("/deadline")
    public ResponseEntity<ApiResponse> getCampaignsNearingDeadline(
            @RequestParam(defaultValue = "7") @Min(value = 1, message = "Days must be positive") int days) {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getCampaignsNearingDeadline(days);
            return ResponseEntity.ok(new ApiResponse("Campaigns nearing deadline retrieved successfully", campaigns));
        } catch (Exception e) {
            log.error("Error retrieving campaigns nearing deadline: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaigns nearing deadline: " + e.getMessage(), null));
        }
    }
}