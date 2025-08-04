package com.aluminate.aluminate_organization_backend.service.pushNotification;

import com.aluminate.aluminate_organization_backend.dto.announcement.AnnouncementRequest;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Notification;
import com.aluminate.aluminate_organization_backend.model.NotificationType;
import com.aluminate.aluminate_organization_backend.repository.MemberRepository;
import com.aluminate.aluminate_organization_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class PushNotificationService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    public int sendNotificationToAllMembers(List<Member> members, AnnouncementRequest announcementRequest) {

        List<Notification> notifications = memberRepository.findAll().stream()

                .map(member -> {
                    Notification notification = new Notification();
                    notification.setTitle(announcementRequest.getTitle());
                    notification.setMessage(announcementRequest.getMessage());
                    notification.setMember(member);
                    notification.setType(NotificationType.SYSTEM);
                    notification.setDate(LocalDateTime.now());
                    return notification;
                }).toList();
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    public void sendNotificationToSelectedGroups(List<Member> members, AnnouncementRequest announcementRequest) {
        List<Notification> notifications = members.stream()
                .map(member -> {
                    Notification notification = new Notification();
                    notification.setTitle(announcementRequest.getTitle());
                    notification.setMessage(announcementRequest.getMessage());
                    notification.setMember(member);
                    notification.setType(NotificationType.SYSTEM);
                    notification.setDate(LocalDateTime.now());
                    return notification;
                }).toList();
        notificationRepository.saveAll(notifications);
    }
}
