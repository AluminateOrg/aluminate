package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.MemberEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberEventRepository extends JpaRepository<MemberEvent, Long> {
    Optional<MemberEvent> findByMemberIdAndEventId(Long memberId, Long eventId);

    @Query("select me from MemberEvent me where me.member.id = :memberId and me.isAttending = true")
    List<MemberEvent> findAllByMemberIdAndAttendingTrue(Long memberId);

    List<MemberEvent> findAllByEventId(Long eventId);
}
