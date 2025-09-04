package com.aluminate.aluminate_organization_backend.chat.security;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public JwtAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = firstNativeHeader(accessor, "Authorization");
            if (!StringUtils.hasText(auth)) {
                // Try query param token (e.g., ws://.../ws?token=Bearer%20xxx)
                String token = firstNativeHeader(accessor, "token");
                if (StringUtils.hasText(token) && !token.startsWith("Bearer ")) {
                    auth = "Bearer " + token;
                } else {
                    auth = token;
                }
            }
            JwtPrincipal principal = jwtService.validateAndExtract(auth);
            Map<String, Object> session = accessor.getSessionAttributes();
            if (session != null) {
                session.put("principal", principal);
            }
        }
        return message;
    }

    private String firstNativeHeader(StompHeaderAccessor accessor, String headerName) {
        if (accessor.getNativeHeader(headerName) != null && !accessor.getNativeHeader(headerName).isEmpty()) {
            return accessor.getNativeHeader(headerName).get(0);
        }
        return null;
    }
}
