package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long> {
    List<MemberGroup> findByMember(Member member);

    @Query("SELECT mg.group.id FROM MemberGroup mg WHERE mg.member.id = :id")
    List<Long> findGroupIdsByMemberId(@Param("id") Long id);
}
