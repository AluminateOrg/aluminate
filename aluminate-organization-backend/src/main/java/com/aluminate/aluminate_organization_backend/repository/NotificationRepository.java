package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Notification;
import com.aluminate.aluminate_organization_backend.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberOrderByDateDesc(Member member);
    List<Notification> findByMemberAndIsReadFalseOrderByDateDesc(Member member);
    List<Notification> findByMemberAndTypeOrderByDateDesc(Member member, NotificationType type);
    long countByMemberAndIsReadFalse(Member member);
}