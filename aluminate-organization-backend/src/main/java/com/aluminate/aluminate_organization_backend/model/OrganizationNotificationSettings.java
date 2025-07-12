package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class OrganizationNotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean emailNotifications;
    private boolean memberJoinNotifications;
    private boolean eventReminderNotifications;
    private boolean donationAlertNotifications;
    private boolean systemUpdateNotifications;
    private boolean weeklyReportNotifications;
}
