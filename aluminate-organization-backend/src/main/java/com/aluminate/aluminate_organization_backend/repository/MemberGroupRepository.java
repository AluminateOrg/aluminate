package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.GroupJoinRequestStatus;
import com.aluminate.aluminate_organization_backend.model.Groups;
import com.aluminate.aluminate_organization_backend.model.MemberGroup;
import com.aluminate.aluminate_organization_backend.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long> {
    List<MemberGroup> findByMember(Member member);

    @Query("SELECT mg.group.id FROM MemberGroup mg WHERE mg.member.id = :id")
    List<Long> findGroupIdsByMemberId(@Param("id") Long id);

    List<MemberGroup> findByGroup_IdIn(List<Long> groupIds);

    boolean existsByMemberAndGroup(Member member, Groups group);

    Optional<MemberGroup> findByMember_IdAndGroup_Id(Long memberId, Long groupId);

    List<MemberGroup> findByRequestStatus(GroupJoinRequestStatus groupJoinRequestStatus);
}
