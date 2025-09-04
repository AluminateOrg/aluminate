package com.aluminate.aluminate_organization_backend.chat.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.aluminate.aluminate_organization_backend.chat.config.ChatProperties;
import com.aluminate.aluminate_organization_backend.chat.dto.MessageResponse;
import com.aluminate.aluminate_organization_backend.chat.dto.PageResponse;
import com.aluminate.aluminate_organization_backend.chat.dto.SendMessageRequest;
import com.aluminate.aluminate_organization_backend.chat.model.Message;
import com.aluminate.aluminate_organization_backend.chat.repository.MessageRepository;
import com.aluminate.aluminate_organization_backend.chat.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MessageRepository repo;
    private final MembershipService membershipService;
    private final StringRedisTemplate redis;
    private final UserService userService;
    private final SimpMessagingTemplate messaging;
    private final ChatProperties props;

    public PageResponse<MessageResponse> getHistory(String orgId, String groupId, int limit, String cursorIso) {
        Assert.hasText(orgId, "orgId required");
        Assert.hasText(groupId, "groupId required");
        if (limit <= 0 || limit > 200) limit = 50;

        List<Message> messages;
        if (StringUtils.hasText(cursorIso)) {
            Instant cursor = parseCursor(cursorIso);
            messages = repo.findByOrgIdAndGroupIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
                    orgId, groupId, cursor, PageRequest.of(0, limit));
        } else {
            messages = repo.findByOrgIdAndGroupIdOrderByCreatedAtDesc(orgId, groupId, PageRequest.of(0, limit));
        }

        // resolve sender names in batch
        Set<String> senderIds = messages.stream().map(Message::getSenderId).collect(Collectors.toSet());
        Map<String, String> names = userService.getDisplayNames(senderIds);

        String nextCursor = messages.size() == limit
                ? messages.get(messages.size() - 1).getCreatedAt().toString()
                : null;

        return PageResponse.<MessageResponse>builder()
                .items(messages.stream().map(m -> toDto(m, names.getOrDefault(m.getSenderId(), m.getSenderId()))).toList())
                .nextCursor(nextCursor)
                .build();
    }

    public MessageResponse sendMessage(String orgId, String groupId, SendMessageRequest req, JwtPrincipal principal) {
        Assert.hasText(req.getContent(), "content required");
        byte[] contentBytes = req.getContent().getBytes(StandardCharsets.UTF_8);
        if (contentBytes.length > props.getMessageMaxBytes()) {
            throw new IllegalArgumentException("Message exceeds max size");
        }

        String userId = principal.getUserId();
        boolean allowed;
        if ("UNIVERSAL".equals(groupId)) {
            allowed = membershipService.isAdmin(orgId, userId);
        } else {
            allowed = membershipService.isMember(orgId, userId, groupId);
        }
        if (!allowed) {
            throw new SecurityException("Forbidden");
        }

        enforceRateLimit(orgId, userId);

        Message m = Message.builder()
                .orgId(orgId)
                .groupId(groupId)
                .senderId(userId)
                .senderRole(principal.isAdmin() ? "ADMIN" : "MEMBER")
                .content(req.getContent())
                .createdAt(Instant.now())
                .meta(Message.Meta.builder().edited(false).pinned(false).build())
                .build();

        m = repo.save(m);

        // publish to Redis for cross-instance fan-out
        publishEvent(orgId, m);

        // also deliver to local WS clients immediately
        String topic = topicFor(orgId, groupId);
        String senderName = userService.getDisplayName(userId);
        MessageResponse dto = toDto(m, senderName);
        messaging.convertAndSend(topic, dto);

        return dto;
    }

    private void publishEvent(String orgId, Message m) {
        try {
            String channel = String.format(props.getRedisPubsubPattern(), orgId);
            String payload = MAPPER.writeValueAsString(toDto(m, userService.getDisplayName(m.getSenderId())));
            redis.convertAndSend(channel, payload);
        } catch (Exception e) {
            // do not fail the send if pubsub fails
        }
    }

    private String topicFor(String orgId, String groupId) {
        return "/topic/org." + orgId + ".group." + groupId;
    }

    private void enforceRateLimit(String orgId, String userId) {
        String secKey = "rate:" + orgId + ":" + userId + ":sec";
        String minKey = "rate:" + orgId + ":" + userId + ":min";

        long sec = redis.opsForValue().increment(secKey);
        if (sec == 1) redis.expire(secKey, java.time.Duration.ofSeconds(1));
        if (sec > props.getRatelimitPerSecond()) {
            throw new TooManyRequestsException("Rate limit exceeded (per second)");
        }

        long min = redis.opsForValue().increment(minKey);
        if (min == 1) redis.expire(minKey, java.time.Duration.ofMinutes(1));
        if (min > props.getRatelimitPerMinute()) {
            throw new TooManyRequestsException("Rate limit exceeded (per minute)");
        }
    }

    private Instant parseCursor(String iso) {
        try {
            return Instant.parse(iso);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    private MessageResponse toDto(Message m, String senderName) {
        return MessageResponse.builder()
                .id(m.getId())
                .orgId(m.getOrgId())
                .groupId(m.getGroupId())
                .senderId(m.getSenderId())
                .senderRole(m.getSenderRole())
                .senderName(senderName)
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String msg) { super(msg); }
    }
}
