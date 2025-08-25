package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.service.donation.IDonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class DonationController {

    private final IDonationService donationService;

    // MEMBER: Get their own donations
    @GetMapping("/member/donations/my-donations/{memberId}")
    public ResponseEntity<ApiResponse> getMyDonations(@PathVariable Long memberId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByMember(memberId);
            return ResponseEntity.ok(new ApiResponse("Donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donations", null));
        }
    }

    // MEMBER: Get donation statistics for a member
    @GetMapping("/member/donations/stats/{memberId}")
    public ResponseEntity<ApiResponse> getDonationStats(@PathVariable Long memberId) {
        try {
            MemberDonationStatsDTO stats = donationService.getDonationStatsByMember(memberId);
            return ResponseEntity.ok(new ApiResponse("Statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve statistics", null));
        }
    }

    // BOTH: Get donations for a specific campaign
    @GetMapping({"/admin/donations/campaign/{campaignId}", "/member/donations/campaign/{campaignId}"})
    public ResponseEntity<ApiResponse> getDonationsByCampaign(@PathVariable Long campaignId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByCampaign(campaignId);
            return ResponseEntity.ok(new ApiResponse("Campaign donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign donations", null));
        }
    }

    // ADMIN: Get all donations
    @GetMapping("/admin/donations/all")
    public ResponseEntity<ApiResponse> getAllDonations() {
        try {
            List<DonationResponseDTO> donations = donationService.getAllDonations();
            return ResponseEntity.ok(new ApiResponse("All donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve all donations", null));
        }
    }

    // ADMIN: Get donation by ID
    @GetMapping("/admin/donations/{donationId}")
    public ResponseEntity<ApiResponse> getDonationById(@PathVariable Long donationId) {
        try {
            DonationResponseDTO donation = donationService.getDonationById(donationId);
            return ResponseEntity.ok(new ApiResponse("Donation retrieved successfully", donation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donation", null));
        }
    }

    // ADMIN: Get donations by status
    @GetMapping("/admin/donations/status/{status}")
    public ResponseEntity<ApiResponse> getDonationsByStatus(@PathVariable String status) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByStatus(status);
            return ResponseEntity.ok(new ApiResponse("Donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donations by status", null));
        }
    }

    // ADMIN: Get donation statistics for all campaigns
    @GetMapping({"/admin/donations/stats"})
    public ResponseEntity<ApiResponse> getAllDonationStats() {
        try {
            DonationStatsDTO stats = donationService.getAllDonationStats();
            return ResponseEntity.ok(new ApiResponse("Overall statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve overall statistics", null));
        }
    }

    // ADMIN: Get donation statistics by campaign
    @GetMapping("/admin/donations/stats/campaign/{campaignId}")
    public ResponseEntity<ApiResponse> getCampaignDonationStats(@PathVariable Long campaignId) {
        try {
            DonationStatsDTO stats = donationService.getDonationStatsByCampaign(campaignId);
            return ResponseEntity.ok(new ApiResponse("Campaign statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign statistics", null));
        }
    }

    // ADMIN: Update donation status (for manual intervention)
    @PutMapping("/admin/donations/{donationId}/status")
    public ResponseEntity<ApiResponse> updateDonationStatus(
            @PathVariable Long donationId,
            @RequestParam String status) {
        try {
            DonationResponseDTO updatedDonation = donationService.updateDonationStatus(donationId, status);
            return ResponseEntity.ok(new ApiResponse("Donation status updated successfully", updatedDonation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update donation status", null));
        }
    }

    // ADMIN: Get recent donations
    @GetMapping("/admin/donations/recent")
    public ResponseEntity<ApiResponse> getRecentDonations(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<DonationResponseDTO> donations = donationService.getRecentDonations(limit);
            return ResponseEntity.ok(new ApiResponse("Recent donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve recent donations", null));
        }
    }

    // ADMIN: Get top donations
    @GetMapping("/admin/donations/top")
    public ResponseEntity<ApiResponse> getTopDonations(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<DonationResponseDTO> donations = donationService.getTopDonations(limit);
            return ResponseEntity.ok(new ApiResponse("Top donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve top donations", null));
        }
    }

    // ADMIN: Get top donors
    @GetMapping("/admin/donations/donors/top")
    public ResponseEntity<ApiResponse> getTopDonors(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<MemberDonationStatsDTO> donors = donationService.getTopDonors(limit);
            return ResponseEntity.ok(new ApiResponse("Top donors retrieved successfully", donors));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve top donors", null));
        }
    }
}