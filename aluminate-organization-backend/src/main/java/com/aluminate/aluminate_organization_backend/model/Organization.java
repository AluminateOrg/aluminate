package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder

public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String organizationName;



    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDate nextRenewalDate;
    private String subdomain;
    private String portalUrl;
    private int maxMemberCount;
    private int currentMemberCount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private boolean isMembershipFree = true;

    @OneToOne
    @JoinColumn(
            name = "admin_id",
            foreignKey = @ForeignKey(
                    name = "fk_admin",
                    foreignKeyDefinition = "FOREIGN KEY (admin_id) REFERENCES admin(id) ON UPDATE CASCADE ON DELETE CASCADE"
            )
    )
    private Admin admin;


}

