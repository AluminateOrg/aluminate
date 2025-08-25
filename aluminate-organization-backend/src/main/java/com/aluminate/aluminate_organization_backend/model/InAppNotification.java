package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "inapp_notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InAppNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String memberId;
    @Column(nullable = false) private String tenantId;
    @Column(nullable = false) private String type;

    @Lob @Column(nullable = false)
    private String payloadJson;

    @Column(nullable = false)
    private boolean readFlag = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
