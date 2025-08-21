package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.model.Donation;

import java.util.List;

public interface IDonationService {

    // Basic CRUD operations
    List<DonationResponseDTO> getDonationsByMember(Long memberId);
    List<DonationResponseDTO> getDonationsByCampaign(Long campaignId);
    List<DonationResponseDTO> getAllDonations();
    DonationResponseDTO getDonationById(Long donationId);

    // Status management
    List<DonationResponseDTO> getDonationsByStatus(String status);
    DonationResponseDTO updateDonationStatus(Long donationId, String status);

    // Statistics
    MemberDonationStatsDTO getDonationStatsByMember(Long memberId);
    DonationStatsDTO getAllDonationStats();
    DonationStatsDTO getDonationStatsByCampaign(Long campaignId);

    // Analytics
    List<DonationResponseDTO> getRecentDonations(int limit);
    List<DonationResponseDTO> getTopDonations(int limit);
    List<MemberDonationStatsDTO> getTopDonors(int limit);

    // Business operations
    Donation createDonation(DonationRequestDTO request);
    boolean processDonationCompletion(String orderId);
    boolean processDonationFailure(String orderId, String reason);
}