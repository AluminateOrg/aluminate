package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByNic(String nic);
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m.email FROM Member m WHERE m.email IS NOT NULL ")
    List<String> findAllEmails();

    List<Member> findAll();

}
