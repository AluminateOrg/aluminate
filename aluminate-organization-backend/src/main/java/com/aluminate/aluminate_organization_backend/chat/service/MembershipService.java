package com.aluminate.aluminate_organization_backend.chat.service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.aluminate.aluminate_organization_backend.chat.config.ChatProperties;
import com.aluminate.aluminate_organization_backend.chat.integration.MembershipClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final MembershipClient membershipClient;
    private final StringRedisTemplate redis;
    private final ChatProperties props;

    public boolean isAdmin(String orgId, String userId) {
        return membershipClient.isAdmin(orgId, userId);
    }

    public boolean isMember(String orgId, String userId, String groupId) {
        if ("UNIVERSAL".equals(groupId)) {
            // readable by all members; enforce membership existence by checking any group membership
            return !getGroupsForUser(orgId, userId).isEmpty() || membershipClient.isAdmin(orgId, userId);
        }
        return getGroupsForUser(orgId, userId).contains(groupId);
    }

    public Set<String> getGroupsForUser(String orgId, String userId) {
        String key = cacheKey(orgId, userId);
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return MAPPER.readValue(cached, new TypeReference<Set<String>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to read membership cache for {}:{} - {}", orgId, userId, e.getMessage());
        }

        Set<String> groups = membershipClient.groupsForUser(userId);
        cacheGroups(orgId, userId, groups);
        return groups;
    }

    public void evictGroups(String orgId, String userId) {
        redis.delete(cacheKey(orgId, userId));
    }

    private void cacheGroups(String orgId, String userId, Set<String> groups) {
        try {
            String key = cacheKey(orgId, userId);
            redis.opsForValue().set(key, MAPPER.writeValueAsString(groups == null ? Collections.emptySet() : groups),
                    Duration.ofSeconds(props.getMembershipCacheTtlSeconds()));
        } catch (Exception e) {
            log.warn("Failed to cache membership for {}:{} - {}", orgId, userId, e.getMessage());
        }
    }

    private String cacheKey(String orgId, String userId) {
        return String.format("org:%s:user:%s:groups", orgId, userId);
    }
}
