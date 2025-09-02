package com.aluminate.aluminate_organization_backend.service.members;

import com.aluminate.aluminate_organization_backend.dto.MemberResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.donation.MemberDonationStatsDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.service.donation.IDonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for member-specific donation operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDonationService {

    private final MemberRepository memberRepository;
    private final IDonationService donationService;

    /**
     * Get member profile with basic information for donation forms
     */
    public MemberResponseDTO getMemberProfile(Long memberId) {
        log.info("Fetching member profile for ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (!member.isActive()) {
            throw new IllegalStateException("Member account is not active");
        }

        return MemberResponseDTO.builder()
                .name(member.getName())
                .nic(member.getNic())
                .phone(member.getPhone())
                .email(member.getEmail())
                .is_active(member.isActive())
                .regNo(member.getRegNo())
                .address(member.getAddress())
                .photoUrl(member.getPhotoUrl())
                .degree(member.getDegree())
                .company(member.getCompany())
                .position(member.getPosition())
                .linkedinUrl(member.getLinkedinUrl())
                .githubUrl(member.getGithubUrl())
                .websiteUrl(member.getWebsiteUrl())
                .batch(member.getBatch())
                .build();
    }

    /**
     * Get member donation statistics
     */
    public MemberDonationStatsDTO getMemberDonationStats(Long memberId) {
        log.info("Fetching donation statistics for member ID: {}", memberId);

        // Validate member exists
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        return donationService.getDonationStatsByMember(memberId);
    }

    /**
     * Validate member can make donations
     */
    public void validateMemberForDonation(Long memberId) {
        log.debug("Validating member {} for donation eligibility", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        if (!member.isActive()) {
            throw new IllegalStateException("Member account is not active. Please contact support.");
        }

        // Add any additional validation logic here
        // For example: membership payment status, organization membership, etc.
        if (member.getOrganization() == null) {
            log.warn("Member {} is not associated with any organization", memberId);
            // You might want to throw an exception here depending on your business rules
        }

        log.debug("Member {} validated successfully for donations", memberId);
    }

    /**
     * Get member basic info for payment processing
     */
    public Member getMemberForPayment(Long memberId) {
        log.debug("Fetching member for payment processing: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        validateMemberForDonation(memberId);

        return member;
    }
}