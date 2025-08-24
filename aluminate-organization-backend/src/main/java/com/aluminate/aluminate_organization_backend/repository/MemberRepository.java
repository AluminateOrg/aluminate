package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNic(String nic);
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
    Optional<Member> findByRegNo(String regNo);
    Optional<Member> findByNic(String nic);

    @Query("SELECT m.email FROM Member m WHERE m.email IS NOT NULL ")
    List<String> findAllEmails();
    List<Member> findByOrganizationIsNull();

    List<Member> findAll();

}
