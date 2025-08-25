package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.model.CampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // Basic finder methods
    boolean existsByTitleAndIsDeletedFalse(String title);
    List<Campaign> findAllByIsDeletedFalse();
    List<Campaign> findAllByIsDeletedFalseAndIsActiveTrue();
    Optional<Campaign> findByIdAndIsDeletedFalse(Long id);

    // Search functionality
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND " +
            "(LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Campaign> searchCampaigns(@Param("searchTerm") String searchTerm, Pageable pageable);

    List<Campaign> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    // Filter by type
    List<Campaign> findAllByTypeAndIsDeletedFalse(CampaignType type);
    List<Campaign> findAllByTypeAndIsDeletedFalseAndIsActiveTrue(CampaignType type);

    // Filter by date ranges
    List<Campaign> findAllByIsDeletedFalseAndEndDateAfter(LocalDate date);
    List<Campaign> findAllByIsDeletedFalseAndEndDateBefore(LocalDate date);
    List<Campaign> findAllByIsDeletedFalseAndStartDateBetween(LocalDate startDate, LocalDate endDate);

    // Filter by goal and raised amounts
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.goal BETWEEN :minGoal AND :maxGoal")
    List<Campaign> findByGoalRange(@Param("minGoal") BigDecimal minGoal, @Param("maxGoal") BigDecimal maxGoal);

    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.raised >= :minRaised")
    List<Campaign> findByMinimumRaisedAmount(@Param("minRaised") BigDecimal minRaised);

    // FIXED STATISTICS QUERIES
    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = false")
    long countActiveCampaigns();

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = false AND c.isActive = true AND c.endDate >= CURRENT_DATE")
    long countActiveCampaignsWithActiveStatus();

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = false AND c.endDate < CURRENT_DATE")
    long countExpiredCampaigns();

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = false AND c.isActive = false AND c.endDate >= CURRENT_DATE")
    long countInactiveCampaigns();

    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.isDeleted = true")
    long countDeletedCampaigns();

    // FIXED SUM QUERIES WITH NULL SAFETY
    @Query("SELECT COALESCE(SUM(c.goal), 0) FROM Campaign c WHERE c.isDeleted = false")
    BigDecimal sumTotalGoals();

    @Query("SELECT COALESCE(SUM(c.raised), 0) FROM Campaign c WHERE c.isDeleted = false")
    BigDecimal sumTotalRaised();

    @Query("SELECT COALESCE(SUM(c.donorCount), 0) FROM Campaign c WHERE c.isDeleted = false")
    Integer sumTotalDonors();

    // Top performing campaigns
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false ORDER BY c.raised DESC")
    List<Campaign> findTopCampaignsByRaisedAmount(Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false ORDER BY c.donorCount DESC")
    List<Campaign> findTopCampaignsByDonorCount(Pageable pageable);

    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.goal > 0 ORDER BY (c.raised / c.goal) DESC")
    List<Campaign> findTopCampaignsByProgress(Pageable pageable);

    // Recent campaigns
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false ORDER BY c.createdAt DESC")
    List<Campaign> findRecentCampaigns(Pageable pageable);

    // Campaigns nearing deadline
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.isActive = true AND c.endDate BETWEEN CURRENT_DATE AND :deadline")
    List<Campaign> findCampaignsNearingDeadline(@Param("deadline") LocalDate deadline);

    // Campaigns by progress percentage
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.goal > 0 AND (c.raised / c.goal * 100) >= :minPercentage")
    List<Campaign> findCampaignsByMinProgress(@Param("minPercentage") double minPercentage);

    // Most popular campaign type
    @Query("SELECT c.type, COUNT(c) as count FROM Campaign c WHERE c.isDeleted = false GROUP BY c.type ORDER BY count DESC")
    List<Object[]> findCampaignTypeStatistics();

    // Custom finder for admin dashboard with filters
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive) AND " +
            "(:startDate IS NULL OR c.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR c.endDate <= :endDate)")
    Page<Campaign> findCampaignsWithFilters(
            @Param("type") CampaignType type,
            @Param("isActive") Boolean isActive,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Find campaigns that need attention (low progress, near deadline)
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND c.isActive = true AND " +
            "c.endDate BETWEEN CURRENT_DATE AND :deadline AND " +
            "c.goal > 0 AND (c.raised / c.goal * 100) < :progressThreshold")
    List<Campaign> findCampaignsNeedingAttention(
            @Param("deadline") LocalDate deadline,
            @Param("progressThreshold") double progressThreshold
    );

    // Find campaigns by status
    @Query("SELECT c FROM Campaign c WHERE c.isDeleted = false AND " +
            "CASE " +
            "   WHEN c.isDeleted = true THEN 'DELETED' " +
            "   WHEN c.endDate < CURRENT_DATE THEN 'EXPIRED' " +
            "   WHEN c.raised >= c.goal THEN 'COMPLETED' " +
            "   WHEN c.isActive = false THEN 'INACTIVE' " +
            "   ELSE 'ACTIVE' " +
            "END = :status")
    List<Campaign> findByStatus(@Param("status") String status);
}