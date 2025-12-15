package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNic(String nic);
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
    Optional<Member> findByRegNo(String regNo);
    Optional<Member> findByNic(String nic);
    Optional<Member> findByPublicSlug(String publicSlug);      // NEW
    boolean existsByPublicSlug(String publicSlug);
    @Query("SELECT m.email FROM Member m WHERE m.email IS NOT NULL ")
    List<String> findAllEmails();
    List<Member> findByOrganizationIsNull();

    List<Member> findAll();

    @Query("SELECT m FROM Member m WHERE " +
            "(:status IS NULL OR m.isActive = :status) AND " +
            "(:search IS NULL OR LOWER(m.name) LIKE :search OR LOWER(m.email) LIKE :search) " +
            "ORDER BY m.id DESC")
    Page<Member> searchMember(@Param("search") String search, @Param("status") Boolean status, Pageable pageable);
}
