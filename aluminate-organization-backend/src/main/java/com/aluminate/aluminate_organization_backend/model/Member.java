package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String nic;
    private String phone;
    private String email;
    private String regNo;
    private String address;
    private String photoUrl;
    private String password;
    private String degree;
    private String company;
    private String position;
    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;
    private int batch;
    @Builder.Default
    private boolean isActive = true;

    /** One member can be a member of multiple groups */
    @OneToMany(mappedBy = "member")
    private Set<MemberGroup> memberGroups = new HashSet<>();

    /** One member can be a member of multiple events */
    @OneToMany(mappedBy = "member")
    private Set<MemberEvent> memberEvents = new HashSet<>();

    /** One member can donate multiple times */
    @OneToMany(mappedBy = "member")
    private Set<Donation> donations = new HashSet<>();

    /** One member can receive multiple notifications */
    @OneToMany(mappedBy = "member")
    private Set<Notification> notifications = new HashSet<>();


    /** Utility method to add donation */
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setMember(this);
    }

    /** Utility method to add notification */
    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setMember(this);
    }


}
