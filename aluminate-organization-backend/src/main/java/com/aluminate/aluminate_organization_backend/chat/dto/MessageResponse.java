package com.aluminate.aluminate_organization_backend.chat.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MessageResponse {
    String id;
    String orgId;
    String groupId;
    String senderId;
    String senderRole;
    String senderName;
    String content;
    Instant createdAt;
}
