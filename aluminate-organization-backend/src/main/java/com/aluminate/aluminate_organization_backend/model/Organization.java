package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder

public class Organization {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String organizationName;
    private int maxMemberCount;
    private int currentMemberCount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private boolean isMembershipFree = true;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<Member> members = new HashSet<>();

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

