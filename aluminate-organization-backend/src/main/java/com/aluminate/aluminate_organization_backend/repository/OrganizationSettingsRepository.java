package com.aluminate.aluminate_organization_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettingsRepository, Long> {
    Optional<OrganizationSettingsRepository> findById(Long id);
}
