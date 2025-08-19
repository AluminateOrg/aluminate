package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.CreateDonationRequest;
import com.aluminate.aluminate_organization_backend.dto.donation.DonationResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashRequest;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentHashResponse;
import com.aluminate.aluminate_organization_backend.dto.payment.PaymentNotificationRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for managing donation operations.
 */
public interface IDonationService {

    /**
     * Creates a new donation record (initially PENDING status).
     */
    DonationResponseDTO createDonation(Long memberId, CreateDonationRequest request);

    /**
     * Updates donation with PayHere payment order ID.
     */
    DonationResponseDTO updatePaymentOrderId(Long donationId, String paymentOrderId);

    /**
     * Generates payment hash for PayHere integration.
     */
    PaymentHashResponse generatePaymentHash(PaymentHashRequest request);

    /**
     * Handles payment notification from PayHere.
     */
    void handlePaymentNotification(PaymentNotificationRequest notification);

    /**
     * Retrieves all donations by a specific member.
     */
    List<DonationResponseDTO> getDonationsByMember(Long memberId);

    /**
     * Retrieves all donations for a specific campaign.
     */
    List<DonationResponseDTO> getDonationsByCampaign(Long campaignId);

    /**
     * Retrieves a donation by its ID.
     */
    DonationResponseDTO getDonationById(Long donationId);

    /**
     * Gets total amount donated by a member.
     */
    BigDecimal getTotalDonatedByMember(Long memberId);

    /**
     * Updates campaign statistics after successful donation.
     */
    void updateCampaignStatistics(Long campaignId);

    /**
     * Retrieves all donations (admin function).
     */
    List<DonationResponseDTO> getAllDonations();
}