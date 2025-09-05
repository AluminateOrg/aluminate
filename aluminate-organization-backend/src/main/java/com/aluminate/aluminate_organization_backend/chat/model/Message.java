package com.aluminate.aluminate_organization_backend.chat.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat message document stored in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
@CompoundIndex(name = "org_group_created_idx", def = "{ 'orgId': 1, 'groupId': 1, 'createdAt': -1 }")
public class Message {

    @Id
    private String id;

    private String orgId;
    /**
     * Group chat id; use "UNIVERSAL" for universal org chat
     */
    private String groupId;

    private String senderId;
    private String senderRole; // ADMIN | MEMBER

    private String content;

    private Instant createdAt;
    private Instant editedAt;
    private Instant deletedAt;

    private Meta meta;

    private List<String> readBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private boolean edited;
        private boolean pinned;
    }
}
