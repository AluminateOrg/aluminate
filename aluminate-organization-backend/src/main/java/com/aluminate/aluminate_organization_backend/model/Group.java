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

public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String category;
    private String name;
    private int maxMembers;
    private int currentMembers;
    private boolean isActive;
    private LocalDate createdDate;

    /** To Access group members via MemberGroup entity */
    @OneToMany(mappedBy = "group")
    private Set<MemberGroup> memberGroups = new HashSet<>();
}
