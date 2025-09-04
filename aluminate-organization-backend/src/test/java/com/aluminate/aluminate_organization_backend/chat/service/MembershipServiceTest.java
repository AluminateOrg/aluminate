package com.aluminate.aluminate_organization_backend.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.aluminate.aluminate_organization_backend.chat.config.ChatProperties;
import com.aluminate.aluminate_organization_backend.chat.integration.MembershipClient;

class MembershipServiceTest {

    @Test
    void cachesGroups() throws Exception {
        MembershipClient client = mock(MembershipClient.class);
        when(client.groupsForUser("user1")).thenReturn(Set.of("g1", "g2"));

        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);

        ChatProperties props = new ChatProperties();
        props.setMembershipCacheTtlSeconds(60);

        MembershipService svc = new MembershipService(client, redis, props);
        Set<String> groups = svc.getGroupsForUser("org1", "user1");

        assertTrue(groups.contains("g2"));
        verify(ops).set(startsWith("org:org1:user:user1:groups"), anyString(), eq(Duration.ofSeconds(60)));
    }
}
