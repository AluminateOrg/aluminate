package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.InAppNotification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    @Query("select n from InAppNotification n where n.memberId = :mid order by n.createdAt desc")
    List<InAppNotification> findAllByMember(@Param("mid") String memberId);

    @Query("select count(n) from InAppNotification n where n.memberId = :mid and n.readFlag = false")
    long countUnread(@Param("mid") String memberId);
}
