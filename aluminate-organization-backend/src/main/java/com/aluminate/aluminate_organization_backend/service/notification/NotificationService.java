package com.aluminate.aluminate_organization_backend.service.notification;

import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createDonationNotification(Member member, Campaign campaign, Donation donation) {
        // Create notification for the donor
        Notification donorNotification = Notification.builder()
                .member(member)
                .type(NotificationType.DONATION)
                .title("Donation Successful")
                .message(String.format("Your donation of LKR %s to '%s' has been processed successfully. Thank you for your generosity!",
                        donation.getAmount().toString(), campaign.getTitle()))
                .date(LocalDateTime.now())
                .isRead(false)
                .build();

        notificationRepository.save(donorNotification);
    }

    @Transactional
    public void createDonationFailedNotification(Member member, Campaign campaign, Donation donation) {
        Notification failedNotification = Notification.builder()
                .member(member)
                .type(NotificationType.WARNING)
                .title("Donation Failed")
                .message(String.format("Your donation of LKR %s to '%s' could not be processed. Please try again or contact support.",
                        donation.getAmount().toString(), campaign.getTitle()))
                .date(LocalDateTime.now())
                .isRead(false)
                .build();

        notificationRepository.save(failedNotification);
    }
}