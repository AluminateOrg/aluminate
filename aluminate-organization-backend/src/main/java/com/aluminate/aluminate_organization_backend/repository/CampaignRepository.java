package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    boolean existsByTitleAndIsDeletedFalse(String title);
    List<Campaign> findAllByIsDeletedFalse();

    // Add this new method for active campaigns
    List<Campaign> findAllByIsDeletedFalseAndIsActiveTrue();
}