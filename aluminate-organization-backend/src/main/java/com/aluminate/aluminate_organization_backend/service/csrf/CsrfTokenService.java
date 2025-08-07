package com.aluminate.aluminate_organization_backend.service.csrf;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
public class CsrfTokenService {
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Logger logger = LoggerFactory.getLogger(CsrfTokenService.class);

    public CsrfTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    public String generateAndStoreToken(String sessionId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String csrfToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        try{
            String redisKey = "csrf:" + sessionId;
            redisTemplate.opsForValue().set(redisKey, csrfToken, Duration.ofDays(1)); // TTL for CSRF
            logger.info("CSRF token generated and stored for session: " + sessionId);

        }catch(Exception e){

            throw new RuntimeException(e);
        }


        return csrfToken;
    }

    public boolean validateToken(String sessionId, String tokenFromClient) {
        String redisKey = "csrf:" + sessionId;

        String storedToken = redisTemplate.opsForValue().get(redisKey);

        boolean isValid = storedToken != null && storedToken.equals(tokenFromClient);

        if (!isValid) {
            logger.error("CSRF TOKEN MISMATCH:\n" +
                            "Session: {}\n" +
                            "Header Token: {}\n" +
                            "Redis Token: {}",
                    sessionId, tokenFromClient, storedToken);
        }

        return isValid;
    }
}

