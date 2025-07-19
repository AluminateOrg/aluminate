package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    private GroupCategory category;

    private String name;
    private int maxMembers;

    @Builder.Default
    private int currentMembers = 0; // current number of members in the group, initialized to 0

    @Builder.Default
    private boolean isActive = true; // true if the group is active, false if it is archived

    @Builder.Default
    private boolean isDeleted = false; // true if the group is deleted, false if it is active

    @Builder.Default
    private LocalDate createdDate = LocalDate.now(); // date when the group was created

    /** To Access group members via MemberGroup entity */
    @OneToMany(mappedBy = "group")
    private Set<MemberGroup> memberGroups = new HashSet<>();
}
