package com.aluminate.aluminate_organization_backend.service.campaign;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignStatsDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.model.CampaignType;
import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"campaigns", "activeCampaigns", "campaignStats"}, allEntries = true)
    public CampaignResponseDTO createCampaign(CreateCampaignRequest request) {
        log.info("Creating campaign with title: {}", request.getTitle());

        validateCampaignRequest(request);

        log.info("type: {}", request.getType());
log.info("type instance of CampaignType: {}", request.getType() instanceof CampaignType);
        Campaign campaign = Campaign.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : "")
                .type(request.getType())
                .goal(request.getGoal())
                .raised(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .endDate(request.getEndDate())
                .donorCount(0)
                .isActive(request.isActive())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign created successfully with ID: {}", savedCampaign.getId());
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"campaigns", "activeCampaigns", "campaignStats"}, allEntries = true)
    public CampaignResponseDTO updateCampaign(Long id, UpdateCampaignRequest request) {
        log.info("Updating campaign with ID: {}", id);

        Campaign campaign = findCampaignById(id);
        validateUpdateRequest(campaign, request);
        updateCampaignFields(campaign, request);

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Campaign updated successfully with ID: {}", id);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "campaigns")
    public List<CampaignResponseDTO> getAllCampaigns() {
        log.debug("Fetching all campaigns");
        return campaignRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "activeCampaigns")
    public List<CampaignResponseDTO> getActiveCampaignsForDonation() {
        log.debug("Fetching active campaigns for donation");
        return campaignRepository.findAllByIsDeletedFalseAndIsActiveTrue()
                .stream()
                .filter(campaign -> !campaign.isExpired())
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponseDTO getCampaignById(Long id) {
        log.debug("Fetching campaign by ID: {}", id);
        Campaign campaign = findCampaignById(id);
        return mapToCampaignResponseDTO(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponseDTO getActiveCampaignById(Long id) {
        log.debug("Fetching active campaign by ID: {}", id);
        Campaign campaign = campaignRepository.findByIdAndIsDeletedFalse(id)
                .filter(c -> c.isActive() && !c.isExpired())
                .orElseThrow(() -> new ResourceNotFoundException("Active campaign not found with id: " + id));
        return mapToCampaignResponseDTO(campaign);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"campaigns", "activeCampaigns", "campaignStats"}, allEntries = true)
    public CampaignResponseDTO updateCampaignStatus(Long id, boolean isActive) {
        log.info("Updating campaign status for ID: {} to {}", id, isActive);

        Campaign campaign = findCampaignById(id);

        if (campaign.isActive() == isActive) {
            throw new IllegalStateException("Campaign is already " + (isActive ? "active" : "inactive"));
        }

        if (isActive && campaign.isExpired()) {
            throw new IllegalStateException("Cannot activate an expired campaign");
        }

        campaign.setActive(isActive);
        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"campaigns", "activeCampaigns", "campaignStats"}, allEntries = true)
    public CampaignResponseDTO deleteCampaign(Long id) {
        log.info("Soft deleting campaign with ID: {}", id);

        Campaign campaign = findCampaignById(id);

        if (campaign.isDeleted()) {
            throw new IllegalStateException("Campaign is already deleted");
        }

        campaign.setDeleted(true);
        campaign.setDeletedAt(LocalDateTime.now());
        campaign.setActive(false);

        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> searchCampaigns(String searchTerm, Pageable pageable) {
        log.debug("Searching campaigns with term: {}", searchTerm);
        return campaignRepository.searchCampaigns(searchTerm, pageable)
                .map(this::mapToCampaignResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> getCampaignsByType(CampaignType type) {
        log.debug("Fetching campaigns by type: {}", type);
        return campaignRepository.findAllByTypeAndIsDeletedFalse(type)
                .stream()
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "campaignStats")
    public CampaignStatsDTO getCampaignStatistics() {
        log.debug("Calculating campaign statistics");

        long totalCampaigns = campaignRepository.countActiveCampaigns();
        long activeCampaigns = campaignRepository.countActiveCampaignsWithActiveStatus();
        long inactiveCampaigns = campaignRepository.countInactiveCampaigns();
        long expiredCampaigns = campaignRepository.countExpiredCampaigns();
        long deletedCampaigns = campaignRepository.countDeletedCampaigns();

        BigDecimal totalGoal = campaignRepository.sumTotalGoals() != null ?
                campaignRepository.sumTotalGoals() : BigDecimal.ZERO;
        BigDecimal totalRaised = campaignRepository.sumTotalRaised() != null ?
                campaignRepository.sumTotalRaised() : BigDecimal.ZERO;
        Integer totalDonors = campaignRepository.sumTotalDonors() != null ?
                campaignRepository.sumTotalDonors() : 0;

        // Calculate average progress
        BigDecimal averageProgress = totalGoal.compareTo(BigDecimal.ZERO) > 0 ?
                totalRaised.divide(totalGoal, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)) :
                BigDecimal.ZERO;

        // Calculate average donation amount
        BigDecimal averageDonationAmount = totalDonors > 0 ?
                totalRaised.divide(new BigDecimal(totalDonors), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        // Get most popular campaign type
        String mostPopularType = getMostPopularCampaignType();

        // Get highest goal and raised amounts
        List<Campaign> topByGoal = campaignRepository.findTopCampaignsByRaisedAmount(Pageable.ofSize(1));
        List<Campaign> topByRaised = campaignRepository.findTopCampaignsByRaisedAmount(Pageable.ofSize(1));

        BigDecimal highestGoal = topByGoal.isEmpty() ? BigDecimal.ZERO : topByGoal.get(0).getGoal();
        BigDecimal highestRaised = topByRaised.isEmpty() ? BigDecimal.ZERO : topByRaised.get(0).getRaised();

        // Count campaigns needing attention
        long campaignsNeedingAttention = campaignRepository
                .findCampaignsNeedingAttention(LocalDate.now().plusDays(7), 25.0).size();

        return CampaignStatsDTO.builder()
                .totalCampaigns(totalCampaigns)
                .activeCampaigns(activeCampaigns)
                .inactiveCampaigns(inactiveCampaigns)
                .expiredCampaigns(expiredCampaigns)
                .deletedCampaigns(deletedCampaigns)
                .totalGoal(totalGoal)
                .totalRaised(totalRaised)
                .totalDonors(totalDonors)
                .averageProgress(averageProgress)
                .averageDonationAmount(averageDonationAmount)
                .mostPopularCampaignType(mostPopularType)
                .highestGoal(highestGoal)
                .highestRaised(highestRaised)
                .campaignsNeedingAttention(campaignsNeedingAttention)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> getRecentCampaigns(int limit) {
        log.debug("Fetching {} recent campaigns", limit);
        return campaignRepository.findRecentCampaigns(Pageable.ofSize(limit))
                .stream()
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> getCampaignsNearingDeadline(int days) {
        log.debug("Fetching campaigns nearing deadline in {} days", days);
        LocalDate deadline = LocalDate.now().plusDays(days);
        return campaignRepository.findCampaignsNearingDeadline(deadline)
                .stream()
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Campaign findCampaignById(Long id) {
        return campaignRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
    }

    private void validateCampaignRequest(CreateCampaignRequest request) {
        if (campaignRepository.existsByTitleAndIsDeletedFalse(request.getTitle().trim())) {
            throw new IllegalArgumentException("Campaign with title '" + request.getTitle() + "' already exists");
        }

        if (request.getEndDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("End date must be at least tomorrow");
        }

        if (request.getGoal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Goal amount must be greater than zero");
        }
    }

    private void validateUpdateRequest(Campaign campaign, UpdateCampaignRequest request) {
        if (request.getTitle() != null && !request.getTitle().trim().equals(campaign.getTitle())) {
            if (campaignRepository.existsByTitleAndIsDeletedFalse(request.getTitle().trim())) {
                throw new IllegalArgumentException("Campaign with title '" + request.getTitle() + "' already exists");
            }
        }

        if (request.getEndDate() != null) {
            LocalDate today = LocalDate.now();
            if (request.getEndDate().isBefore(today) && campaign.getEndDate().isAfter(today)) {
                throw new IllegalArgumentException("Cannot set end date to past for active campaigns");
            }
        }

        if (request.getGoal() != null) {
            if (request.getGoal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Goal amount must be greater than zero");
            }
            if (request.getGoal().compareTo(campaign.getRaised()) < 0) {
                throw new IllegalArgumentException("Goal cannot be less than amount already raised (LKR " +
                        campaign.getRaised() + ")");
            }
        }
    }

    private void updateCampaignFields(Campaign campaign, UpdateCampaignRequest request) {
        if (request.getTitle() != null) {
            campaign.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription().trim());
        }
        if (request.getType() != null) {
            campaign.setType(request.getType());
        }
        if (request.getGoal() != null) {
            campaign.setGoal(request.getGoal());
        }
        if (request.getEndDate() != null) {
            campaign.setEndDate(request.getEndDate());
        }
        campaign.setUpdatedAt(LocalDateTime.now());
    }

    private String getMostPopularCampaignType() {
        List<Object[]> typeStats = campaignRepository.findCampaignTypeStatistics();
        if (typeStats.isEmpty()) {
            return "GENERAL";
        }
        return ((CampaignType) typeStats.get(0)[0]).name();
    }

    private String determineCampaignStatus(Campaign campaign) {
        if (campaign.isDeleted()) return "DELETED";
        if (!campaign.isActive()) return "INACTIVE";
        if (campaign.isExpired()) return "EXPIRED";
        if (campaign.isGoalAchieved()) return "COMPLETED";
        return "ACTIVE";
    }

    private CampaignResponseDTO mapToCampaignResponseDTO(Campaign campaign) {
        return CampaignResponseDTO.builder()
                .id(campaign.getId())
                .title(campaign.getTitle())
                .description(campaign.getDescription())
                .type(campaign.getType())
                .goal(campaign.getGoal())
                .raised(campaign.getRaised())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .donorCount(campaign.getDonorCount())
                .isActive(campaign.isActive())
                .isDeleted(campaign.isDeleted())
                .deletedAt(campaign.getDeletedAt())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .progressPercentage(campaign.getProgressPercentage())
                .isExpired(campaign.isExpired())
                .canAcceptDonations(campaign.canAcceptDonations())
                .daysRemaining(campaign.getDaysRemaining())
                .status(determineCampaignStatus(campaign))
                .remainingAmount(campaign.getRemainingAmount())
                .isGoalAchieved(campaign.isGoalAchieved())
                .build();
    }
}