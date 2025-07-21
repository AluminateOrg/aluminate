package com.aluminate.aluminate_organization_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aluminate.aluminate_organization_backend.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // Optionally add custom methods like:
    boolean existsByEmail(String email);
}
