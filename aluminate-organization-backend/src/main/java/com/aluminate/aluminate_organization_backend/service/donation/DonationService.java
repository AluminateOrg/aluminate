package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.model.Donation;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
import com.aluminate.aluminate_organization_backend.repository.DonationRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DonationService implements IDonationService {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final CampaignRepository campaignRepository;
    private final Logger log = LoggerFactory.getLogger(DonationService.class);

    @Override
    public List<DonationResponseDTO> getDonationsByMember(Long memberId) {
        List<Donation> donations = donationRepository.findByMemberId(memberId);
        log.info("Found DONATION HISTORY {} donations for member ID: {}", donations.size(), memberId);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<DonationResponseDTO> getDonationsByCampaign(Long campaignId) {
        List<Donation> donations = donationRepository.findByCampaignId(campaignId);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<DonationResponseDTO> getAllDonations() {
        List<Donation> donations = donationRepository.findAll();
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public DonationResponseDTO getDonationById(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));
        return convertToResponseDTO(donation);
    }

    @Override
    public List<DonationResponseDTO> getDonationsByStatus(String status) {
        List<Donation> donations = donationRepository.findAll().stream()
                .filter(d -> status.equalsIgnoreCase(d.getStatus()))
                .toList();
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DonationResponseDTO updateDonationStatus(Long donationId, String status) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found with id: " + donationId));

        donation.setStatus(status);
        donation.setUpdatedAt(LocalDateTime.now());
        Donation savedDonation = donationRepository.save(donation);
        return convertToResponseDTO(savedDonation);
    }

    @Override
    public MemberDonationStatsDTO getDonationStatsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        List<Donation> donations = donationRepository.findByMemberId(memberId);

        log.info("Calculating donation stats for member ID: {}, donation count: {}", memberId, donations.size());
        BigDecimal totalAmount = donations.stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = donations.stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .count();

        long pendingCount = donations.stream()
                .filter(d -> Donation.DonationStatus.PENDING.equals(d.getStatus()))
                .count();

        long campaignsSupported = donations.stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .map(d -> d.getCampaign().getId())
                .distinct()
                .count();

        return MemberDonationStatsDTO.builder()
                .memberId(memberId)
                .memberName(member.getName())
                .totalDonated(totalAmount)
                .totalDonations(donations.size())
                .completedDonations(completedCount)
                .pendingDonations(pendingCount)
                .averageDonation(completedCount > 0 ?
                        totalAmount.divide(BigDecimal.valueOf(completedCount)) : BigDecimal.ZERO)
                .campaignsSupported(campaignsSupported)
                .build();
    }

    @Override
    public DonationStatsDTO getAllDonationStats() {
        List<Donation> allDonations = donationRepository.findAll();

        List<Donation> completedDonations = allDonations.stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .toList();

        BigDecimal totalAmount = completedDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedDonations.size();
        long pendingCount = allDonations.stream()
                .filter(d -> Donation.DonationStatus.PENDING.equals(d.getStatus()))
                .count();
        long failedCount = allDonations.stream()
                .filter(d -> Donation.DonationStatus.FAILED.equals(d.getStatus()))
                .count();

        long uniqueDonors = completedDonations.stream()
                .map(d -> d.getMember().getId())
                .distinct()
                .count();

        long campaignsWithDonations = completedDonations.stream()
                .map(d -> d.getCampaign().getId())
                .distinct()
                .count();

        BigDecimal highestDonation = completedDonations.stream()
                .map(Donation::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowestDonation = completedDonations.stream()
                .map(Donation::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return DonationStatsDTO.builder()
                .totalDonated(totalAmount)
                .totalDonations(allDonations.size())
                .completedDonations(completedCount)
                .pendingDonations(pendingCount)
                .failedDonations(failedCount)
                .uniqueDonors(uniqueDonors)
                .campaignsWithDonations(campaignsWithDonations)
                .averageDonation(completedCount > 0 ?
                        totalAmount.divide(BigDecimal.valueOf(completedCount)) : BigDecimal.ZERO)
                .highestDonation(highestDonation)
                .lowestDonation(lowestDonation)
                .build();
    }

    @Override
    public DonationStatsDTO getDonationStatsByCampaign(Long campaignId) {
        List<Donation> campaignDonations = donationRepository.findByCampaignId(campaignId);

        List<Donation> completedDonations = campaignDonations.stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .toList();

        BigDecimal totalAmount = completedDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedDonations.size();
        long pendingCount = campaignDonations.stream()
                .filter(d -> Donation.DonationStatus.PENDING.equals(d.getStatus()))
                .count();
        long failedCount = campaignDonations.stream()
                .filter(d -> Donation.DonationStatus.FAILED.equals(d.getStatus()))
                .count();

        long uniqueDonors = completedDonations.stream()
                .map(d -> d.getMember().getId())
                .distinct()
                .count();

        BigDecimal highestDonation = completedDonations.stream()
                .map(Donation::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowestDonation = completedDonations.stream()
                .map(Donation::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return DonationStatsDTO.builder()
                .totalDonated(totalAmount)
                .totalDonations(campaignDonations.size())
                .completedDonations(completedCount)
                .pendingDonations(pendingCount)
                .failedDonations(failedCount)
                .uniqueDonors(uniqueDonors)
                .campaignsWithDonations(1L)
                .averageDonation(completedCount > 0 ?
                        totalAmount.divide(BigDecimal.valueOf(completedCount)) : BigDecimal.ZERO)
                .highestDonation(highestDonation)
                .lowestDonation(lowestDonation)
                .build();
    }

    @Override
    public List<DonationResponseDTO> getRecentDonations(int limit) {
        List<Donation> donations = donationRepository.findAll().stream()
                .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                .limit(limit)
                .toList();
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<DonationResponseDTO> getTopDonations(int limit) {
        List<Donation> donations = donationRepository.findAll().stream()
                .filter(d -> Donation.DonationStatus.COMPLETED.equals(d.getStatus()))
                .sorted((d1, d2) -> d2.getAmount().compareTo(d1.getAmount()))
                .limit(limit)
                .toList();
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<MemberDonationStatsDTO> getTopDonors(int limit) {
        return memberRepository.findAll().stream()
                .map(member -> getDonationStatsByMember(member.getId()))
                .filter(stats -> stats.getTotalDonated().compareTo(BigDecimal.ZERO) > 0)
                .sorted((s1, s2) -> s2.getTotalDonated().compareTo(s1.getTotalDonated()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Donation createDonation(DonationRequestDTO request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted() && c.isActive())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found or inactive"));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Donation donation = Donation.builder()
                .campaign(campaign)
                .member(member)
                .amount(request.getAmount())
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isAnonymous(request.isAnonymous())
                .message(request.getMessage())
                .status(Donation.DonationStatus.PENDING)
                .paymentStatus(Donation.PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAYHERE")
                .build();

        return donationRepository.save(donation);
    }

    @Override
    @Transactional
    public boolean processDonationCompletion(String orderId) {
        return donationRepository.findByPaymentOrderId(orderId)
                .map(donation -> {
                    donation.setStatus(Donation.DonationStatus.COMPLETED);
                    donation.setPaymentStatus(Donation.PaymentStatus.COMPLETED);
                    donation.setUpdatedAt(LocalDateTime.now());

                    // Update campaign statistics
                    Campaign campaign = donation.getCampaign();
                    campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
                    campaign.setDonorCount(campaign.getDonorCount() + 1);
                    campaignRepository.save(campaign);

                    donationRepository.save(donation);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean processDonationFailure(String orderId, String reason) {
        return donationRepository.findByPaymentOrderId(orderId)
                .map(donation -> {
                    donation.setStatus(Donation.DonationStatus.FAILED);
                    donation.setPaymentStatus(Donation.PaymentStatus.FAILED);
                    donation.setUpdatedAt(LocalDateTime.now());
                    donationRepository.save(donation);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Donation getDonationByOrderId(String orderId) {
        return donationRepository.findByPaymentOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found for order: " + orderId));
    }

    private DonationResponseDTO convertToResponseDTO(Donation donation) {
        return DonationResponseDTO.builder()
                .id(donation.getId())
                .amount(donation.getAmount())
                .date(donation.getDate())
                .campaignId(donation.getCampaign().getId())
                .campaignTitle(donation.getCampaign().getTitle())
                .memberId(donation.getMember().getId())
                .memberName(donation.isAnonymous() ? "Anonymous" : donation.getMember().getName())
                .isAnonymous(donation.isAnonymous())
                .message(donation.getMessage())
                .paymentMethod(donation.getPaymentMethod())
                .status(donation.getStatus())
                .paymentStatus(donation.getPaymentStatus())
                .transactionId(donation.getTransactionId())
                .paymentOrderId(donation.getPaymentOrderId())
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }
}