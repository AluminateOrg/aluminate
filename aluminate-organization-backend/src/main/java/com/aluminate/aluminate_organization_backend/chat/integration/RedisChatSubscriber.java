package com.aluminate.aluminate_organization_backend.chat.integration;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.aluminate.aluminate_organization_backend.chat.dto.MessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatSubscriber implements MessageListener {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            MessageResponse msg = MAPPER.readValue(payload, MessageResponse.class);
            String topic = "/topic/org." + msg.getOrgId() + ".group." + msg.getGroupId();
            messagingTemplate.convertAndSend(topic, msg);
        } catch (Exception e) {
            log.warn("Failed to process chat event: {}", e.getMessage());
        }
    }
}
