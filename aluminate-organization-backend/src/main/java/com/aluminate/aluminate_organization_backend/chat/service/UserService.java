package com.aluminate.aluminate_organization_backend.chat.service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.aluminate.aluminate_organization_backend.chat.config.ChatProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final JdbcTemplate jdbc;
    private final StringRedisTemplate redis;
    private final ChatProperties props;

    // Return display name for a single user (fallback to userId if not found)
    public String getDisplayName(String userId) {
        return getDisplayNames(Set.of(userId)).getOrDefault(userId, userId);
    }

    // Batch-resolve display names. Uses Redis cache and a single DB query for missing ids.
    public Map<String, String> getDisplayNames(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();

        Map<String, String> result = new HashMap<>();
        List<String> keys = userIds.stream().map(id -> cacheKey(id)).collect(Collectors.toList());
        List<String> cached = redis.opsForValue().multiGet(keys);

        // collect missing ids
        int i = 0;
        Set<String> missing = userIds.stream().collect(Collectors.toSet());
        for (String val : cached) {
            String id = userIds.stream().skip(i).findFirst().orElse(null); // placeholder index logic fixed below
            i++;
        }

        // Simpler: iterate same order as keys
        i = 0;
        String[] idsArr = userIds.toArray(new String[0]);
        for (String val : cached) {
            String id = idsArr[i++];
            if (val != null) {
                result.put(id, val);
                missing.remove(id);
            }
        }

        if (!missing.isEmpty()) {
            try {
                // Convert missing ids to Longs, skip non-numeric ids
                List<Long> idsLong = missing.stream()
                        .map(s -> {
                            try { return Long.valueOf(s); }
                            catch (NumberFormatException ex) { return null; }
                        })
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());

                if (!idsLong.isEmpty()) {
                    String inSql = idsLong.stream().map(ii -> "?").collect(Collectors.joining(","));
                    // use the Member table and the 'name' column (matches Member.java)
                    String sql = "select id, name from member where id in (" + inSql + ")";
                    Object[] args = idsLong.toArray(new Object[0]);
                    List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
                    for (Map<String, Object> row : rows) {
                        String id = String.valueOf(row.get("id"));
                        String display = row.get("name") == null ? id : String.valueOf(row.get("name"));
                        result.put(id, display);
                        // cache individual values
                        try {
                            redis.opsForValue().set(cacheKey(id), display,
                                    Duration.ofSeconds(props.getMembershipCacheTtlSeconds()));
                        } catch (Exception e) {
                            log.warn("Failed to cache user display for {}: {}", id, e.getMessage());
                        }
                        missing.remove(id);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to query user display names: {}", e.getMessage());
            }
        }

        // fallback for any still-missing ids -> use id
        for (String id : missing) {
            result.put(id, id);
        }

        return result;
    }

    private String cacheKey(String userId) {
        return "user:" + userId + ":display";
    }
}