package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByCampaignId(Long campaignId);
    List<Donation> findByMemberId(Long memberId);

    // Add these new methods:
    Optional<Donation> findByPaymentOrderId(String paymentOrderId);
    Optional<Donation> findByTransactionId(String transactionId);

    @Query("SELECT d FROM Donation d WHERE d.campaign.id = :campaignId AND d.status = :status")
    List<Donation> findByCampaignIdAndStatus(@Param("campaignId") Long campaignId, @Param("status") String status);
}