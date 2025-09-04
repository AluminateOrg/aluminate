package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(
            name = "fk_member",
            foreignKeyDefinition = "FOREIGN KEY (member_id) REFERENCES member(id) ON UPDATE CASCADE ON DELETE CASCADE"
    ))
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Groups group;

    private boolean isApproved; // true if the member has been approved to join the group

    @Enumerated(EnumType.STRING)
    private GroupJoinRequestStatus requestStatus;

    private String role; // "ADMIN", "MEMBER"

    private LocalDateTime requestDate;
    private LocalDateTime responseDate;
}
