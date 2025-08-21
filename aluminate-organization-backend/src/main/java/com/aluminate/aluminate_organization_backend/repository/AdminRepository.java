package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findAdminByEmail(String email);
}
