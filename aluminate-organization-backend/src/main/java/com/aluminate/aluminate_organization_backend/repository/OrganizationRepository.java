package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByOrganizationName(String name);
    Organization findByAdminId(Long adminId);

    @Query(value = "SELECT * FROM organization ORDER BY id ASC LIMIT 1", nativeQuery = true)
    Organization getFirstRow();
}
