package com.aluminate.aluminate_organization_backend.service.campaign;

import com.aluminate.aluminate_organization_backend.dto.campaign.CreateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.UpdateCampaignRequest;
import com.aluminate.aluminate_organization_backend.dto.campaign.CampaignResponseDTO;
import com.aluminate.aluminate_organization_backend.exception.ResourceNotFoundException;
import com.aluminate.aluminate_organization_backend.model.Campaign;
import com.aluminate.aluminate_organization_backend.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing campaign operations.
 */
@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {
    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public CampaignResponseDTO createCampaign(CreateCampaignRequest request) {
        // Check if campaign with same title already exists
        if (campaignRepository.existsByTitleAndIsDeletedFalse(request.getTitle())) {
            throw new IllegalArgumentException("Campaign with title '" + request.getTitle() + "' already exists");
        }

        // Validate end date is in the future
        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("End date must be in the future");
        }

        // Validate goal is positive
        if (request.getGoal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Goal amount must be greater than zero");
        }

        Campaign campaign = Campaign.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .goal(request.getGoal())
                .raised(BigDecimal.ZERO)
                .startDate(LocalDate.now())
                .endDate(request.getEndDate())
                .donorCount(0)
                .isActive(request.isActive()) // Use isActive from request
                .isDeleted(false)
                .build();

        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponseDTO updateCampaign(Long id, UpdateCampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        // Validate title uniqueness (only if title is being changed)
        if (request.getTitle() != null && !request.getTitle().equals(campaign.getTitle())) {
            if (campaignRepository.existsByTitleAndIsDeletedFalse(request.getTitle())) {
                throw new IllegalArgumentException("Campaign with title '" + request.getTitle() + "' already exists");
            }
        }

        // Validate end date is in the future (only if changing to a future date)
        if (request.getEndDate() != null) {
            LocalDate today = LocalDate.now();
            // Allow setting end date to past only if current end date is already in the past
            if (request.getEndDate().isBefore(today) && campaign.getEndDate().isAfter(today)) {
                throw new IllegalArgumentException("End date must be in the future for active campaigns");
            }
        }

        // Validate goal is not less than already raised amount
        if (request.getGoal() != null) {
            if (request.getGoal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Goal amount must be greater than zero");
            }
            if (request.getGoal().compareTo(campaign.getRaised()) < 0) {
                throw new IllegalArgumentException("Goal cannot be less than the amount already raised (LKR " +
                        campaign.getRaised() + ")");
            }
        }

        // Update only the provided fields
        if (request.getTitle() != null) {
            campaign.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            campaign.setDescription(request.getDescription());
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

        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> getAllCampaigns() {
        return campaignRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::mapToCampaignResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponseDTO getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return mapToCampaignResponseDTO(campaign);
    }

    @Override
    @Transactional
    public CampaignResponseDTO updateCampaignStatus(Long id, boolean isActive) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (campaign.isActive() == isActive) {
            throw new IllegalStateException("Campaign is already " + (isActive ? "active" : "inactive"));
        }

        campaign.setActive(isActive);
        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponseDTO deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (campaign.isDeleted()) {
            throw new IllegalStateException("Campaign is already deleted");
        }

        campaign.setDeleted(true);
        campaign.setDeletedAt(LocalDateTime.now());
        campaign.setActive(false); // Also deactivate the campaign
        Campaign savedCampaign = campaignRepository.save(campaign);
        return mapToCampaignResponseDTO(savedCampaign);
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
                .build();
    }
}