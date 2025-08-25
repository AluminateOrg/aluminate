package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.donation.IDonationService;
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
import java.util.Map;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class DonationController {

    private final IDonationService donationService;

    // ==========================================
    // MEMBER ENDPOINTS
    // ==========================================

    /**
     * Get member's own donations
     */
    @GetMapping("/member/donations/my-donations/{memberId}")
    public ResponseEntity<ApiResponse> getMyDonations(
            @PathVariable @Min(value = 1, message = "Member ID must be positive") Long memberId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        try {
            log.info("Fetching donations for member: {}", memberId);

            Page<DonationResponseDTO> donations = donationService.getDonationsByMemberPaged(memberId, pageable);

            return ResponseEntity.ok(new ApiResponse("Donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving donations for member: {}", memberId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donations: " + e.getMessage(), null));
        }
    }

    /**
     * Get donation statistics for a member
     */
    @GetMapping("/member/donations/stats/{memberId}")
    public ResponseEntity<ApiResponse> getDonationStats(
            @PathVariable @Min(value = 1, message = "Member ID must be positive") Long memberId) {
        try {
            log.info("Fetching donation statistics for member: {}", memberId);

            MemberDonationStatsDTO stats = donationService.getDonationStatsByMember(memberId);

            return ResponseEntity.ok(new ApiResponse("Statistics retrieved successfully", stats));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error retrieving statistics for member: {}", memberId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve statistics: " + e.getMessage(), null));
        }
    }

    /**
     * Get member's donation history with filtering
     */
    @GetMapping("/member/donations/history/{memberId}")
    public ResponseEntity<ApiResponse> getDonationHistory(
            @PathVariable Long memberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long campaignId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        try {
            log.info("Fetching donation history for member: {} with filters - status: {}, campaignId: {}",
                    memberId, status, campaignId);

            Page<DonationResponseDTO> donations = donationService.getDonationHistory(memberId, status, campaignId, pageable);

            return ResponseEntity.ok(new ApiResponse("Donation history retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving donation history for member: {}", memberId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donation history: " + e.getMessage(), null));
        }
    }

    // ==========================================
    // SHARED ENDPOINTS (MEMBER & ADMIN)
    // ==========================================

    /**
     * Get donations for a specific campaign
     */
    @GetMapping({"/admin/donations/campaign/{campaignId}", "/member/donations/campaign/{campaignId}"})
    public ResponseEntity<ApiResponse> getDonationsByCampaign(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long campaignId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        try {
            log.info("Fetching donations for campaign: {}", campaignId);

            Page<DonationResponseDTO> donations = donationService.getDonationsByCampaignPaged(campaignId, pageable);

            return ResponseEntity.ok(new ApiResponse("Campaign donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving donations for campaign: {}", campaignId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign donations: " + e.getMessage(), null));
        }
    }

    /**
     * Get donation by ID
     */
    @GetMapping({"/admin/donations/{donationId}", "/member/donations/{donationId}"})
    public ResponseEntity<ApiResponse> getDonationById(
            @PathVariable @Min(value = 1, message = "Donation ID must be positive") Long donationId) {
        try {
            log.info("Fetching donation by ID: {}", donationId);

            DonationResponseDTO donation = donationService.getDonationById(donationId);

            return ResponseEntity.ok(new ApiResponse("Donation retrieved successfully", donation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error retrieving donation: {}", donationId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donation: " + e.getMessage(), null));
        }
    }

    // ==========================================
    // ADMIN ONLY ENDPOINTS
    // ==========================================

    /**
     * Get all donations with pagination and filtering
     */
    @GetMapping("/admin/donations/all")
    public ResponseEntity<ApiResponse> getAllDonations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        try {
            log.info("Fetching all donations with filters - status: {}, campaignId: {}, memberId: {}, search: {}",
                    status, campaignId, memberId, search);

            Page<DonationResponseDTO> donations = donationService.getAllDonationsWithFilters(
                    status, campaignId, memberId, search, pageable);

            return ResponseEntity.ok(new ApiResponse("All donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving all donations: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve all donations: " + e.getMessage(), null));
        }
    }

    /**
     * Get donations by status
     */
    @GetMapping("/admin/donations/status/{status}")
    public ResponseEntity<ApiResponse> getDonationsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        try {
            log.info("Fetching donations by status: {}", status);

            Page<DonationResponseDTO> donations = donationService.getDonationsByStatusPaged(status, pageable);

            return ResponseEntity.ok(new ApiResponse("Donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving donations by status: {}", status, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donations by status: " + e.getMessage(), null));
        }
    }

    /**
     * Update donation status (for manual intervention)
     */
    @PutMapping("/admin/donations/{donationId}/status")
    public ResponseEntity<ApiResponse> updateDonationStatus(
            @PathVariable @Min(value = 1, message = "Donation ID must be positive") Long donationId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        try {
            log.info("Updating donation status for ID: {} to status: {} with reason: {}",
                    donationId, status, reason);

            DonationResponseDTO updatedDonation = donationService.updateDonationStatus(donationId, status, reason);

            return ResponseEntity.ok(new ApiResponse("Donation status updated successfully", updatedDonation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating donation status for ID: {}", donationId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update donation status: " + e.getMessage(), null));
        }
    }

    /**
     * Get overall donation statistics
     */
    @GetMapping("/admin/donations/stats")
    public ResponseEntity<ApiResponse> getAllDonationStats() {
        try {
            log.info("Fetching overall donation statistics");

            DonationStatsDTO stats = donationService.getAllDonationStats();

            return ResponseEntity.ok(new ApiResponse("Overall statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving overall donation statistics: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve overall statistics: " + e.getMessage(), null));
        }
    }

    /**
     * Get donation statistics by campaign
     */
    @GetMapping("/admin/donations/stats/campaign/{campaignId}")
    public ResponseEntity<ApiResponse> getCampaignDonationStats(
            @PathVariable @Min(value = 1, message = "Campaign ID must be positive") Long campaignId) {
        try {
            log.info("Fetching donation statistics for campaign: {}", campaignId);

            DonationStatsDTO stats = donationService.getDonationStatsByCampaign(campaignId);

            return ResponseEntity.ok(new ApiResponse("Campaign statistics retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Error retrieving donation statistics for campaign: {}", campaignId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign statistics: " + e.getMessage(), null));
        }
    }

    /**
     * Get recent donations
     */
    @GetMapping("/admin/donations/recent")
    public ResponseEntity<ApiResponse> getRecentDonations(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be positive") int limit) {
        try {
            log.info("Fetching {} recent donations", limit);

            List<DonationResponseDTO> donations = donationService.getRecentDonations(limit);

            return ResponseEntity.ok(new ApiResponse("Recent donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving recent donations: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve recent donations: " + e.getMessage(), null));
        }
    }

    /**
     * Get top donations
     */
    @GetMapping("/admin/donations/top")
    public ResponseEntity<ApiResponse> getTopDonations(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be positive") int limit) {
        try {
            log.info("Fetching top {} donations", limit);

            List<DonationResponseDTO> donations = donationService.getTopDonations(limit);

            return ResponseEntity.ok(new ApiResponse("Top donations retrieved successfully", donations));
        } catch (Exception e) {
            log.error("Error retrieving top donations: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve top donations: " + e.getMessage(), null));
        }
    }

    /**
     * Get top donors
     */
    @GetMapping("/admin/donations/donors/top")
    public ResponseEntity<ApiResponse> getTopDonors(
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Limit must be positive") int limit) {
        try {
            log.info("Fetching top {} donors", limit);

            List<MemberDonationStatsDTO> donors = donationService.getTopDonors(limit);

            return ResponseEntity.ok(new ApiResponse("Top donors retrieved successfully", donors));
        } catch (Exception e) {
            log.error("Error retrieving top donors: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve top donors: " + e.getMessage(), null));
        }
    }

    /**
     * Get donation trends over time
     */
    @GetMapping("/admin/donations/trends")
    public ResponseEntity<ApiResponse> getDonationTrends(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "day") String groupBy) {
        try {
            log.info("Fetching donation trends for {} days grouped by {}", days, groupBy);

            List<DonationTrendDTO> trends = donationService.getDonationTrends(days, groupBy);

            return ResponseEntity.ok(new ApiResponse("Donation trends retrieved successfully", trends));
        } catch (Exception e) {
            log.error("Error retrieving donation trends: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donation trends: " + e.getMessage(), null));
        }
    }

    /**
     * Export donations to CSV
     */
    @GetMapping("/admin/donations/export")
    public ResponseEntity<ApiResponse> exportDonations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long campaignId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("Exporting donations with filters - status: {}, campaignId: {}, startDate: {}, endDate: {}",
                    status, campaignId, startDate, endDate);

            String csvData = donationService.exportDonationsToCSV(status, campaignId, startDate, endDate);

            return ResponseEntity.ok(new ApiResponse("Donations exported successfully", csvData));
        } catch (Exception e) {
            log.error("Error exporting donations: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to export donations: " + e.getMessage(), null));
        }
    }

    // ==========================================
    // UTILITY ENDPOINTS
    // ==========================================

    /**
     * Get donation status options
     */
    @GetMapping({"/admin/donations/status-options", "/member/donations/status-options"})
    public ResponseEntity<ApiResponse> getDonationStatusOptions() {
        try {
            List<String> statusOptions = List.of(
                    "PENDING", "COMPLETED", "FAILED", "CANCELLED", "REFUNDED"
            );

            return ResponseEntity.ok(new ApiResponse("Status options retrieved successfully", statusOptions));
        } catch (Exception e) {
            log.error("Error retrieving status options: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve status options", null));
        }
    }

    /**
     * Health check for donation service
     */
    @GetMapping({"/admin/donations/health", "/member/donations/health"})
    public ResponseEntity<ApiResponse> healthCheck() {
        try {
            long totalDonations = donationService.getTotalDonationCount();

            return ResponseEntity.ok(new ApiResponse("Donation service is healthy",
                    Map.of("totalDonations", totalDonations, "status", "UP", "timestamp", System.currentTimeMillis())));
        } catch (Exception e) {
            log.error("Health check failed: ", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Donation service health check failed", null));
        }
    }
}