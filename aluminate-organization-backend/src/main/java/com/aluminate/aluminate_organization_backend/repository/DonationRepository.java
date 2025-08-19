package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.model.Donation;
import com.aluminate.aluminate_organization_backend.model.DonationStatus;
import com.aluminate.aluminate_organization_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByMemberOrderByCreatedAtDesc(Member member);

    List<Donation> findByCampaignOrderByCreatedAtDesc(Campaign campaign);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.campaign = :campaign AND d.status = 'COMPLETED'")
    BigDecimal getTotalRaisedByCampaign(@Param("campaign") Campaign campaign);

    @Query("SELECT COUNT(DISTINCT d.member) FROM Donation d WHERE d.campaign = :campaign AND d.status = 'COMPLETED'")
    int getUniqueDonorCountByCampaign(@Param("campaign") Campaign campaign);

    Optional<Donation> findByPaymentOrderId(String paymentOrderId);

    List<Donation> findByStatus(DonationStatus status);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.member = :member AND d.status = 'COMPLETED'")
    BigDecimal getTotalDonatedByMember(@Param("member") Member member);

    @Query("SELECT COUNT(d) FROM Donation d WHERE d.member = :member AND d.status = 'COMPLETED'")
    int getCompletedDonationCountByMember(@Param("member") Member member);
}