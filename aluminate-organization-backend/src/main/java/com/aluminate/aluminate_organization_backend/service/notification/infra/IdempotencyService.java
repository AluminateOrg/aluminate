package com.aluminate.aluminate_organization_backend.service.notification.infra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {

    @Autowired(required = false)
    private StringRedisTemplate redis; // optional; falls back to memory if Redis is down

    private final Map<String, Long> mem = new ConcurrentHashMap<>();
    private static final long TTL_MS = Duration.ofDays(7).toMillis();

    public boolean seen(String key) {
        long now = System.currentTimeMillis();
        try {
            if (redis != null) {
                Boolean set = redis.opsForValue().setIfAbsent("notif:dedupe:"+key, "1", Duration.ofDays(7));
                return set != null && !set;
            }
        } catch (Exception ignored) {}
        // in-memory fallback
        mem.entrySet().removeIf(e -> now - e.getValue() > TTL_MS);
        Long prev = mem.putIfAbsent(key, now);
        return prev != null;
    }
}
