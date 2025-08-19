package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.PaymentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOptionRepository extends JpaRepository<PaymentOption, Long> {

    List<PaymentOption> findByMemberAndIsActiveTrue(Member member);

    Optional<PaymentOption> findByMemberAndIsDefaultTrueAndIsActiveTrue(Member member);

    @Query("SELECT COUNT(p) FROM PaymentOption p WHERE p.member = :member AND p.isActive = true")
    int countActivePaymentOptionsByMember(@Param("member") Member member);

    @Query("UPDATE PaymentOption p SET p.isDefault = false WHERE p.member = :member AND p.id != :excludeId")
    void clearDefaultPaymentOptionsForMember(@Param("member") Member member, @Param("excludeId") Long excludeId);

    @Query("UPDATE PaymentOption p SET p.isDefault = false WHERE p.member = :member")
    void clearAllDefaultPaymentOptionsForMember(@Param("member") Member member);
}