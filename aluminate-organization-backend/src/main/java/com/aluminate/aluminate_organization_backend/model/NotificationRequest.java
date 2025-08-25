package com.aluminate.aluminate_organization_backend.model;

import lombok.*;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationRequest {
    private String messageId;
    private String tenantId;
    private String memberId;
    private Set<String> channels;
    private String type;
    private String template;
    private Map<String, Object> data;
    private String dedupeKey;
    private Instant createdAt;

    public static NotificationRequest of(
            String tenantId, String memberId, Set<String> channels,
            String type, String template, Map<String,Object> data, String dedupeKey) {
        return NotificationRequest.builder()
                .messageId(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .memberId(memberId)
                .channels(channels)
                .type(type)
                .template(template)
                .data(data)
                .dedupeKey(dedupeKey)
                .createdAt(Instant.now())
                .build();
    }
}
