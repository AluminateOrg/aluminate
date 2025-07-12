package com.aluminate.aluminate_organization_backend.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(of = {"id"})

public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String category;
    private String title;
    private String message;
    private LocalDate date;
    private boolean isRead;
}
