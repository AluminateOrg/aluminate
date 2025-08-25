package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {

    // ==========================================
    // BASIC FINDER METHODS
    // ==========================================

    List<Donation> findByCampaignId(Long campaignId);
    List<Donation> findByMemberId(Long memberId);

    Page<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId, Pageable pageable);
    Page<Donation> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Donation> findAllByOrderByCreatedAtDesc();

    // ==========================================
    // PAYMENT RELATED METHODS
    // ==========================================

    Optional<Donation> findByPaymentOrderId(String paymentOrderId);
    Optional<Donation> findByTransactionId(String transactionId);

    // ==========================================
    // STATUS BASED QUERIES
    // ==========================================

    List<Donation> findByStatusOrderByCreatedAtDesc(String status);
    Page<Donation> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT d FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status ORDER BY d.createdAt DESC")
    List<Donation> findByCampaignIdAndStatusOrderByCreatedAtDesc(@Param("campaignId") Long campaignId, @Param("status") String status);

    @Query("SELECT d FROM Donation d WHERE d.member.id = :memberId AND d.status = :status ORDER BY d.createdAt DESC")
    List<Donation> findByMemberIdAndStatusOrderByCreatedAtDesc(@Param("memberId") Long memberId, @Param("status") String status);

    // ==========================================
    // ANALYTICS AND STATISTICS
    // ==========================================

    @Query(value = "SELECT * FROM donation WHERE status = :status ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Donation> findTopByOrderByCreatedAtDesc(@Param("limit") int limit);

    @Query(value = "SELECT * FROM donation WHERE status = :status ORDER BY amount DESC LIMIT :limit", nativeQuery = true)
    List<Donation> findTopByStatusOrderByAmountDesc(@Param("status") String status, @Param("limit") int limit);

    // Date range queries
    List<Donation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Donation> findByCreatedAtAfter(LocalDateTime startDate);
    List<Donation> findByCreatedAtAfterAndStatus(LocalDateTime startDate, String status);

    // Statistics queries
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.status = 'COMPLETED'")
    Optional<java.math.BigDecimal> sumCompletedDonationAmounts();

    @Query("SELECT COUNT(DISTINCT d.member.id) FROM Donation d WHERE d.status = 'COMPLETED'")
    long countDistinctDonors();

    @Query("SELECT COUNT(DISTINCT d.campaign.id) FROM Donation d WHERE d.status = 'COMPLETED'")
    long countDistinctCampaigns();

    // Campaign specific statistics
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'COMPLETED'")
    long countCompletedDonationsByCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = 'COMPLETED'")
    Optional<java.math.BigDecimal> sumCompletedDonationAmountsByCampaign(@Param("campaignId") Long campaignId);

    // Member specific statistics
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.member.id = :memberId AND d.status = 'COMPLETED'")
    long countCompletedDonationsByMember(@Param("memberId") Long memberId);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.member.id = :memberId AND d.status = 'COMPLETED'")
    Optional<java.math.BigDecimal> sumCompletedDonationAmountsByMember(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(DISTINCT d.campaign.id) FROM Donation d WHERE d.member.id = :memberId AND d.status = 'COMPLETED'")
    long countDistinctCampaignsByMember(@Param("memberId") Long memberId);

    // ==========================================
    // TRENDING AND TIME-BASED QUERIES
    // ==========================================

    @Query("SELECT d FROM Donation d WHERE d.createdAt >= :startDate AND d.status = 'COMPLETED' ORDER BY d.createdAt")
    List<Donation> findRecentCompletedDonations(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT DATE(d.createdAt) as donationDate, COUNT(d) as donationCount, SUM(d.amount) as totalAmount " +
            "FROM Donation d WHERE d.createdAt >= :startDate AND d.status = 'COMPLETED' " +
            "GROUP BY DATE(d.createdAt) ORDER BY donationDate")
    List<Object[]> getDailyDonationTrends(@Param("startDate") LocalDateTime startDate);

    // FIXED: Monthly donation trends query with proper type casting
    @Query(value = "SELECT CONCAT(YEAR(created_at), '-', LPAD(MONTH(created_at), 2, '0')) as donationMonth, " +
            "COUNT(*) as donationCount, SUM(amount) as totalAmount " +
            "FROM donation WHERE created_at >= :startDate AND status = 'COMPLETED' " +
            "GROUP BY YEAR(created_at), MONTH(created_at) ORDER BY donationMonth",
            nativeQuery = true)
    List<Object[]> getMonthlyDonationTrends(@Param("startDate") LocalDateTime startDate);

    // ==========================================
    // SEARCH QUERIES
    // ==========================================

    @Query("SELECT d FROM Donation d WHERE " +
            "(LOWER(d.campaign.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.member.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY d.createdAt DESC")
    List<Donation> searchDonations(@Param("search") String search);

    @Query("SELECT d FROM Donation d WHERE " +
            "(LOWER(d.campaign.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.member.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY d.createdAt DESC")
    Page<Donation> searchDonations(@Param("search") String search, Pageable pageable);

    // ==========================================
    // COMPLEX FILTERING QUERIES
    // ==========================================

    @Query("SELECT d FROM Donation d WHERE " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:campaignId IS NULL OR d.campaign.id = :campaignId) AND " +
            "(:memberId IS NULL OR d.member.id = :memberId) AND " +
            "(:startDate IS NULL OR d.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR d.createdAt <= :endDate) " +
            "ORDER BY d.createdAt DESC")
    Page<Donation> findDonationsWithFilters(
            @Param("status") String status,
            @Param("campaignId") Long campaignId,
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==========================================
    // REPORTING QUERIES
    // ==========================================

    @Query("SELECT d.campaign.title as campaignTitle, COUNT(d) as donationCount, " +
            "SUM(d.amount) as totalAmount, AVG(d.amount) as averageAmount " +
            "FROM Donation d WHERE d.status = 'COMPLETED' " +
            "GROUP BY d.campaign.id, d.campaign.title " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getCampaignDonationSummary();

    @Query("SELECT DATE(d.createdAt) as donationDate, d.campaign.title as campaignTitle, " +
            "COUNT(d) as donationCount, SUM(d.amount) as totalAmount " +
            "FROM Donation d WHERE d.status = 'COMPLETED' AND d.createdAt >= :startDate " +
            "GROUP BY DATE(d.createdAt), d.campaign.id, d.campaign.title " +
            "ORDER BY donationDate DESC, totalAmount DESC")
    List<Object[]> getDailyDonationReport(@Param("startDate") LocalDateTime startDate);

    // ==========================================
    // TOP PERFORMERS
    // ==========================================

    @Query("SELECT d.member.id as memberId, d.member.name as memberName, " +
            "COUNT(d) as donationCount, SUM(d.amount) as totalAmount " +
            "FROM Donation d WHERE d.status = 'COMPLETED' " +
            "GROUP BY d.member.id, d.member.name " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getTopDonorsSummary(Pageable pageable);
}