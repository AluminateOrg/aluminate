package com.aluminate.aluminate_organization_backend.chat.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.aluminate.aluminate_organization_backend.chat.dto.MessageResponse;
import com.aluminate.aluminate_organization_backend.chat.dto.SendMessageRequest;
import com.aluminate.aluminate_organization_backend.chat.security.JwtPrincipal;
import com.aluminate.aluminate_organization_backend.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/org/{orgId}/group/{groupId}/send")
    public MessageResponse send(
            @DestinationVariable String orgId,
            @DestinationVariable String groupId,
            @Payload SendMessageRequest payload,
            SimpMessageHeaderAccessor headers,
            @Header(name = "Authorization", required = false) String authHeader) {

        JwtPrincipal principal = (JwtPrincipal) headers.getSessionAttributes().get("principal");
        if (principal == null) {
            // As fallback, try header if present (e.g. from CONNECT)
            throw new IllegalArgumentException("Missing principal");
        }
        if (!orgId.equals(principal.getOrgId())) {
            throw new IllegalArgumentException("orgId mismatch");
        }
        return chatService.sendMessage(orgId, groupId, payload, principal);
    }
}
