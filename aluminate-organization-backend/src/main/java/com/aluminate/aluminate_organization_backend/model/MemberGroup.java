package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = {"id"})

public class MemberGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Groups group;

    private boolean isApproved; // true if the member has been approved to join the group
    private String role; // "ADMIN", "MEMBER"
}
