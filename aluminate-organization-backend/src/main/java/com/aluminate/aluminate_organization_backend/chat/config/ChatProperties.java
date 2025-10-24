package com.aluminate.aluminate_organization_backend.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class ChatProperties {
    private String websocketPath = "/ws";
    private String redisPubsubPattern = "chat:org:%s:events";
    private int membershipCacheTtlSeconds = 60;
    private int messageMaxBytes = 8192;
    private int ratelimitPerSecond = 5;
    private int ratelimitPerMinute = 100;
}
