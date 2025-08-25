package com.aluminate.aluminate_organization_backend.service.donation;

import com.aluminate.aluminate_organization_backend.dto.donation.*;
import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.model.Donation;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
import com.aluminate.aluminate_organization_backend.repository.DonationRepository;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService implements IDonationService {

    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final CampaignRepository campaignRepository;

    // ==========================================
    // BASIC CRUD OPERATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsByMember(Long memberId) {
        log.debug("Fetching donations for member: {}", memberId);
        List<Donation> donations = donationRepository.findByMemberId(memberId);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getDonationsByMemberPaged(Long memberId, Pageable pageable) {
        log.debug("Fetching paged donations for member: {}", memberId);
        Page<Donation> donations = donationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return donations.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsByCampaign(Long campaignId) {
        log.debug("Fetching donations for campaign: {}", campaignId);
        List<Donation> donations = donationRepository.findByCampaignId(campaignId);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getDonationsByCampaignPaged(Long campaignId, Pageable pageable) {
        log.debug("Fetching paged donations for campaign: {}", campaignId);
        Page<Donation> donations = donationRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId, pageable);
        return donations.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getAllDonations() {
        log.debug("Fetching all donations");
        List<Donation> donations = donationRepository.findAllByOrderByCreatedAtDesc();
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getAllDonationsWithFilters(String status, Long campaignId, Long memberId, String search, Pageable pageable) {
        log.debug("Fetching filtered donations - status: {}, campaignId: {}, memberId: {}, search: {}",
                status, campaignId, memberId, search);

        Specification<Donation> spec = buildDonationSpecification(status, campaignId, memberId, search);
        Page<Donation> donations = donationRepository.findAll(spec, pageable);
        return donations.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationResponseDTO getDonationById(Long donationId) {
        log.debug("Fetching donation by ID: {}", donationId);
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));
        return convertToResponseDTO(donation);
    }

    // ==========================================
    // STATUS MANAGEMENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getDonationsByStatus(String status) {
        log.debug("Fetching donations by status: {}", status);
        List<Donation> donations = donationRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getDonationsByStatusPaged(String status, Pageable pageable) {
        log.debug("Fetching paged donations by status: {}", status);
        Page<Donation> donations = donationRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(), pageable);
        return donations.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional
    public DonationResponseDTO updateDonationStatus(Long donationId, String status, String reason) {
        log.info("Updating donation status - ID: {}, status: {}, reason: {}", donationId, status, reason);

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

        String oldStatus = donation.getStatus();
        donation.setStatus(status.toUpperCase());
        donation.setUpdatedAt(LocalDateTime.now());

        // Handle campaign statistics based on status change
        updateCampaignStatistics(donation, oldStatus, status.toUpperCase());

        Donation savedDonation = donationRepository.save(donation);

        log.info("Donation status updated successfully - ID: {}, from: {} to: {}",
                donationId, oldStatus, status);

        return convertToResponseDTO(savedDonation);
    }

    // ==========================================
    // STATISTICS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public MemberDonationStatsDTO getDonationStatsByMember(Long memberId) {
        log.debug("Calculating donation statistics for member: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        List<Donation> donations = donationRepository.findByMemberId(memberId);

        BigDecimal totalAmount = donations.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = donations.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .count();

        long pendingCount = donations.stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .count();

        long campaignsSupported = donations.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
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
                        totalAmount.divide(BigDecimal.valueOf(completedCount), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .campaignsSupported(campaignsSupported)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DonationStatsDTO getAllDonationStats() {
        log.debug("Calculating overall donation statistics");

        List<Donation> allDonations = donationRepository.findAll();

        List<Donation> completedDonations = allDonations.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .toList();

        BigDecimal totalAmount = completedDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedDonations.size();
        long pendingCount = allDonations.stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .count();
        long failedCount = allDonations.stream()
                .filter(d -> "FAILED".equals(d.getStatus()))
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
                        totalAmount.divide(BigDecimal.valueOf(completedCount), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .highestDonation(highestDonation)
                .lowestDonation(lowestDonation)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DonationStatsDTO getDonationStatsByCampaign(Long campaignId) {
        log.debug("Calculating donation statistics for campaign: {}", campaignId);

        List<Donation> campaignDonations = donationRepository.findByCampaignId(campaignId);

        List<Donation> completedDonations = campaignDonations.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .toList();

        BigDecimal totalAmount = completedDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedDonations.size();
        long pendingCount = campaignDonations.stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .count();
        long failedCount = campaignDonations.stream()
                .filter(d -> "FAILED".equals(d.getStatus()))
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
                        totalAmount.divide(BigDecimal.valueOf(completedCount), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO)
                .highestDonation(highestDonation)
                .lowestDonation(lowestDonation)
                .build();
    }

    // ==========================================
    // ANALYTICS AND REPORTS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getRecentDonations(int limit) {
        log.debug("Fetching {} recent donations", limit);
        List<Donation> donations = donationRepository.findTopByOrderByCreatedAtDesc(limit);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationResponseDTO> getTopDonations(int limit) {
        log.debug("Fetching top {} donations by amount", limit);
        List<Donation> donations = donationRepository.findTopByStatusOrderByAmountDesc("COMPLETED", limit);
        return donations.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberDonationStatsDTO> getTopDonors(int limit) {
        log.debug("Fetching top {} donors", limit);
        return memberRepository.findAll().stream()
                .map(member -> getDonationStatsByMember(member.getId()))
                .filter(stats -> stats.getTotalDonated().compareTo(BigDecimal.ZERO) > 0)
                .sorted((s1, s2) -> s2.getTotalDonated().compareTo(s1.getTotalDonated()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationResponseDTO> getDonationHistory(Long memberId, String status, Long campaignId, Pageable pageable) {
        log.debug("Fetching donation history for member: {} with filters", memberId);

        Specification<Donation> spec = buildDonationSpecification(status, campaignId, memberId, null);
        Page<Donation> donations = donationRepository.findAll(spec, pageable);
        return donations.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationTrendDTO> getDonationTrends(int days, String groupBy) {
        log.debug("Calculating donation trends for {} days grouped by {}", days, groupBy);

        LocalDate startDate = LocalDate.now().minusDays(days);
        List<Donation> donations = donationRepository.findByCreatedAtAfterAndStatus(
                startDate.atStartOfDay(), "COMPLETED");

        Map<String, List<Donation>> groupedDonations;

        if ("month".equalsIgnoreCase(groupBy)) {
            groupedDonations = donations.stream()
                    .collect(Collectors.groupingBy(d ->
                            d.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        } else if ("week".equalsIgnoreCase(groupBy)) {
            groupedDonations = donations.stream()
                    .collect(Collectors.groupingBy(d ->
                            d.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-'W'ww"))));
        } else {
            // Default to day
            groupedDonations = donations.stream()
                    .collect(Collectors.groupingBy(d ->
                            d.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }

        return groupedDonations.entrySet().stream()
                .map(entry -> {
                    List<Donation> periodDonations = entry.getValue();
                    BigDecimal totalAmount = periodDonations.stream()
                            .map(Donation::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DonationTrendDTO.builder()
                            .period(entry.getKey())
                            .donationCount(periodDonations.size())
                            .totalAmount(totalAmount)
                            .averageAmount(periodDonations.isEmpty() ? BigDecimal.ZERO :
                                    totalAmount.divide(BigDecimal.valueOf(periodDonations.size()), 2, BigDecimal.ROUND_HALF_UP))
                            .uniqueDonors(periodDonations.stream()
                                    .map(d -> d.getMember().getId())
                                    .distinct()
                                    .count())
                            .build();
                })
                .sorted((t1, t2) -> t1.getPeriod().compareTo(t2.getPeriod()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String exportDonationsToCSV(String status, Long campaignId, String startDate, String endDate) {
        log.debug("Exporting donations to CSV with filters");

        // Build specification for filtering
        Specification<Donation> spec = buildExportSpecification(status, campaignId, startDate, endDate);
        List<Donation> donations = donationRepository.findAll(spec);

        StringBuilder csv = new StringBuilder();
        // CSV Header
        csv.append("ID,Amount,Date,Campaign,Member,Status,Payment Status,Payment Method,Transaction ID,Is Anonymous,Message,Created At\n");

        // CSV Data
        for (Donation donation : donations) {
            csv.append(String.format("%d,%.2f,%s,%s,%s,%s,%s,%s,%s,%s,\"%s\",%s\n",
                    donation.getId(),
                    donation.getAmount(),
                    donation.getDate(),
                    donation.getCampaign().getTitle(),
                    donation.isAnonymous() ? "Anonymous" : donation.getMember().getName(),
                    donation.getStatus(),
                    donation.getPaymentStatus(),
                    donation.getPaymentMethod(),
                    donation.getTransactionId() != null ? donation.getTransactionId() : "",
                    donation.isAnonymous(),
                    donation.getMessage() != null ? donation.getMessage().replace("\"", "\"\"") : "",
                    donation.getCreatedAt()
            ));
        }

        return csv.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDonationCount() {
        return donationRepository.count();
    }

    // ==========================================
    // BUSINESS OPERATIONS
    // ==========================================

    @Override
    @Transactional
    public Donation createDonation(DonationRequestDTO request) {
        log.info("Creating donation for campaign: {} member: {} amount: {}",
                request.getCampaignId(), request.getMemberId(), request.getAmount());

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .filter(c -> !c.isDeleted() && c.isActive())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found or inactive"));

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Donation donation = Donation.builder()
                .campaign(campaign)
                .member(member)
                .amount(request.getAmount())
                .date(LocalDate.now())
                .donationDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .isAnonymous(request.isAnonymous())
                .message(request.getMessage())
                .status("PENDING")
                .paymentStatus("PENDING")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAYHERE")
                .build();

        return donationRepository.save(donation);
    }

    @Override
    @Transactional
    public boolean processDonationCompletion(String orderId) {
        log.info("Processing donation completion for order: {}", orderId);

        return donationRepository.findByPaymentOrderId(orderId)
                .map(donation -> {
                    donation.setStatus("COMPLETED");
                    donation.setPaymentStatus("COMPLETED");
                    donation.setUpdatedAt(LocalDateTime.now());

                    // Update campaign statistics
                    Campaign campaign = donation.getCampaign();
                    campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
                    campaign.setDonorCount(campaign.getDonorCount() + 1);
                    campaignRepository.save(campaign);

                    donationRepository.save(donation);

                    log.info("Donation completion processed successfully for order: {}", orderId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean processDonationFailure(String orderId, String reason) {
        log.info("Processing donation failure for order: {} reason: {}", orderId, reason);

        return donationRepository.findByPaymentOrderId(orderId)
                .map(donation -> {
                    donation.setStatus("FAILED");
                    donation.setPaymentStatus("FAILED");
                    donation.setUpdatedAt(LocalDateTime.now());
                    donationRepository.save(donation);

                    log.info("Donation failure processed successfully for order: {}", orderId);
                    return true;
                })
                .orElse(false);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

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

    private Specification<Donation> buildDonationSpecification(String status, Long campaignId, Long memberId, String search) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (status != null && !status.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("status"), status.toUpperCase()));
            }

            if (campaignId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("campaign").get("id"), campaignId));
            }

            if (memberId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("member").get("id"), memberId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                var searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("campaign").get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("member").get("name")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("message")), searchPattern)
                );
                predicates = criteriaBuilder.and(predicates, searchPredicate);
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return predicates;
        };
    }

    private Specification<Donation> buildExportSpecification(String status, Long campaignId, String startDate, String endDate) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (status != null && !status.trim().isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("status"), status.toUpperCase()));
            }

            if (campaignId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("campaign").get("id"), campaignId));
            }

            if (startDate != null && !startDate.trim().isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("date"), start));
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                LocalDate end = LocalDate.parse(endDate);
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("date"), end));
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            return predicates;
        };
    }

    private void updateCampaignStatistics(Donation donation, String oldStatus, String newStatus) {
        Campaign campaign = donation.getCampaign();

        // If changing from COMPLETED to something else, reverse the statistics
        if ("COMPLETED".equals(oldStatus) && !"COMPLETED".equals(newStatus)) {
            campaign.setRaised(campaign.getRaised().subtract(donation.getAmount()));
            campaign.setDonorCount(Math.max(0, campaign.getDonorCount() - 1));
            campaignRepository.save(campaign);
            log.info("Reversed campaign statistics for donation: {}", donation.getId());
        }

        // If changing from something else to COMPLETED, add to statistics
        if (!"COMPLETED".equals(oldStatus) && "COMPLETED".equals(newStatus)) {
            campaign.setRaised(campaign.getRaised().add(donation.getAmount()));
            campaign.setDonorCount(campaign.getDonorCount() + 1);
            campaignRepository.save(campaign);
            log.info("Updated campaign statistics for donation: {}", donation.getId());
        }
    }
}