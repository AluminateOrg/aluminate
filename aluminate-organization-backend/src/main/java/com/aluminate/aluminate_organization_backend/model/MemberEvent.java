package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = {"id"})

public class MemberEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false,
    foreignKey = @ForeignKey(
            name = "fk_memberevent_member",
            foreignKeyDefinition = "FOREIGN KEY (member_id) REFERENCES member(id) ON UPDATE CASCADE ON DELETE CASCADE"
    ))
    private Member member;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private boolean isAttending; // true if the member is attending the event
    private boolean setRSVP; // true if the member has set their RSVP to attend the event
}
