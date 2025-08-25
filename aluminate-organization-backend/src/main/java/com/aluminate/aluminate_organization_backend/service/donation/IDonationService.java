package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDonationService {

    // ==========================================
    // BASIC CRUD OPERATIONS
    // ==========================================

    /**
     * Get donations by member ID
     */
    List<DonationResponseDTO> getDonationsByMember(Long memberId);

    /**
     * Get donations by member ID with pagination
     */
    Page<DonationResponseDTO> getDonationsByMemberPaged(Long memberId, Pageable pageable);

    /**
     * Get donations by campaign ID
     */
    List<DonationResponseDTO> getDonationsByCampaign(Long campaignId);

    /**
     * Get donations by campaign ID with pagination
     */
    Page<DonationResponseDTO> getDonationsByCampaignPaged(Long campaignId, Pageable pageable);

    /**
     * Get all donations
     */
    List<DonationResponseDTO> getAllDonations();

    /**
     * Get all donations with filters and pagination
     */
    Page<DonationResponseDTO> getAllDonationsWithFilters(String status, Long campaignId,
                                                         Long memberId, String search, Pageable pageable);

    /**
     * Get donation by ID
     */
    DonationResponseDTO getDonationById(Long donationId);

    // ==========================================
    // STATUS MANAGEMENT
    // ==========================================

    /**
     * Get donations by status
     */
    List<DonationResponseDTO> getDonationsByStatus(String status);

    /**
     * Get donations by status with pagination
     */
    Page<DonationResponseDTO> getDonationsByStatusPaged(String status, Pageable pageable);

    /**
     * Update donation status with reason
     */
    DonationResponseDTO updateDonationStatus(Long donationId, String status, String reason);

    // ==========================================
    // STATISTICS
    // ==========================================

    /**
     * Get donation statistics by member
     */
    MemberDonationStatsDTO getDonationStatsByMember(Long memberId);

    /**
     * Get overall donation statistics
     */
    DonationStatsDTO getAllDonationStats();

    /**
     * Get donation statistics by campaign
     */
    DonationStatsDTO getDonationStatsByCampaign(Long campaignId);

    // ==========================================
    // ANALYTICS AND REPORTS
    // ==========================================

    /**
     * Get recent donations
     */
    List<DonationResponseDTO> getRecentDonations(int limit);

    /**
     * Get top donations by amount
     */
    List<DonationResponseDTO> getTopDonations(int limit);

    /**
     * Get top donors
     */
    List<MemberDonationStatsDTO> getTopDonors(int limit);

    /**
     * Get donation history for a member with filters
     */
    Page<DonationResponseDTO> getDonationHistory(Long memberId, String status,
                                                 Long campaignId, Pageable pageable);

    /**
     * Get donation trends over time
     */
    List<DonationTrendDTO> getDonationTrends(int days, String groupBy);

    /**
     * Export donations to CSV
     */
    String exportDonationsToCSV(String status, Long campaignId, String startDate, String endDate);

    /**
     * Get total donation count
     */
    long getTotalDonationCount();

    // ==========================================
    // BUSINESS OPERATIONS
    // ==========================================

    /**
     * Create a new donation
     */
    Donation createDonation(DonationRequestDTO request);

    /**
     * Process donation completion
     */
    boolean processDonationCompletion(String orderId);

    /**
     * Process donation failure
     */
    boolean processDonationFailure(String orderId, String reason);
}