package com.aluminate.aluminate_organization_backend.controller;

import com.aluminate.aluminate_organization_backend.dto.donation.CreateDonationRequest;
import com.aluminate.aluminate_organization_backend.dto.donation.DonationResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashResponse;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentNotificationRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentOrderUpdateRequest;
import com.aluminate.aluminate_organization_backend.dto.response.ApiResponse;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.service.donation.IDonationService;
import com.aluminate.aluminate_organization_backend.service.campaign.ICampaignService;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

/**
 * Controller for managing donation-related operations.
 * Handles member donations to campaigns and PayHere payment integration.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/donation")
public class DonationController {

    private final IDonationService donationService;
    private final ICampaignService campaignService;

    @GetMapping("/campaigns/active")
    public ResponseEntity<ApiResponse> getActiveCampaigns() {
        try {
            List<CampaignResponseDTO> campaigns = campaignService.getActiveCampaignsForDonation();
            return ResponseEntity.ok(new ApiResponse("Active campaigns retrieved successfully", campaigns));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve active campaigns: " + e.getMessage(), null));
        }
    }

    /**
     * Creates a new donation (initially PENDING status).
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createDonation(
            @RequestParam Long memberId,
            @RequestBody CreateDonationRequest request) {
        try {
            DonationResponseDTO donation = donationService.createDonation(memberId, request);
            return ResponseEntity.status(CREATED)
                    .body(new ApiResponse("Donation created successfully", donation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to create donation: " + e.getMessage(), null));
        }
    }

    /**
     * Updates donation with PayHere payment order ID.
     * THIS WAS THE MISSING ENDPOINT!
     */
    @PutMapping("/{donationId}/payment-order")
    public ResponseEntity<ApiResponse> updatePaymentOrder(
            @PathVariable Long donationId,
            @RequestBody PaymentOrderUpdateRequest request) {
        try {
            DonationResponseDTO donation = donationService.updatePaymentOrderId(donationId, request.getPaymentOrderId());
            return ResponseEntity.ok(new ApiResponse("Payment order ID updated successfully", donation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update payment order: " + e.getMessage(), null));
        }
    }

    /**
     * Generates payment hash for PayHere integration.
     */
    @PostMapping("/payment/generate-hash")
    public ResponseEntity<ApiResponse> generatePaymentHash(@RequestBody PaymentHashRequest request) {
        try {
            PaymentHashResponse hashResponse = donationService.generatePaymentHash(request);
            return ResponseEntity.ok(new ApiResponse("Payment hash generated successfully", hashResponse));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to generate payment hash: " + e.getMessage(), null));
        }
    }

    /**
     * Handles payment notification from PayHere (webhook endpoint).
     */
    @PostMapping("/payment/notify")
    public ResponseEntity<String> handlePaymentNotification(@RequestBody PaymentNotificationRequest notification) {
        try {
            donationService.handlePaymentNotification(notification);
            return ResponseEntity.ok("Payment notification processed successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body("Donation not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(BAD_REQUEST).body("Invalid payment notification");
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body("Failed to process payment notification");
        }
    }

    /**
     * Retrieves all donations by a specific member.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse> getDonationsByMember(@PathVariable Long memberId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByMember(memberId);
            return ResponseEntity.ok(new ApiResponse("Member donations retrieved successfully", donations));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve member donations: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all donations for a specific campaign.
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<ApiResponse> getDonationsByCampaign(@PathVariable Long campaignId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByCampaign(campaignId);
            return ResponseEntity.ok(new ApiResponse("Campaign donations retrieved successfully", donations));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve campaign donations: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves a specific donation by ID.
     */
    @GetMapping("/{donationId}")
    public ResponseEntity<ApiResponse> getDonationById(@PathVariable Long donationId) {
        try {
            DonationResponseDTO donation = donationService.getDonationById(donationId);
            return ResponseEntity.ok(new ApiResponse("Donation retrieved successfully", donation));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donation: " + e.getMessage(), null));
        }
    }

    /**
     * Gets total amount donated by a member.
     */
    @GetMapping("/member/{memberId}/total")
    public ResponseEntity<ApiResponse> getTotalDonatedByMember(@PathVariable Long memberId) {
        try {
            BigDecimal total = donationService.getTotalDonatedByMember(memberId);
            return ResponseEntity.ok(new ApiResponse("Total donation amount retrieved successfully", total));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve total donation amount: " + e.getMessage(), null));
        }
    }

    /**
     * Retrieves all donations (admin function).
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllDonations() {
        try {
            List<DonationResponseDTO> donations = donationService.getAllDonations();
            return ResponseEntity.ok(new ApiResponse("All donations retrieved successfully", donations));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to retrieve donations: " + e.getMessage(), null));
        }
    }

    /**
     * Updates campaign statistics manually (admin function).
     */
    @PutMapping("/campaign/{campaignId}/update-statistics")
    public ResponseEntity<ApiResponse> updateCampaignStatistics(@PathVariable Long campaignId) {
        try {
            donationService.updateCampaignStatistics(campaignId);
            return ResponseEntity.ok(new ApiResponse("Campaign statistics updated successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Failed to update campaign statistics: " + e.getMessage(), null));
        }
    }
}