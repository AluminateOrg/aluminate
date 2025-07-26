package com.aluminate.aluminate_organization_backend.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDate registrationDeadline;
    private BigDecimal price;
    @Column(name = "is_public")
    @JsonProperty("public")
    private boolean isPublic;
    private boolean requiresApproval;

    @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "event")
    private Set<MemberEvent> memberEvents = new HashSet<>();

}
