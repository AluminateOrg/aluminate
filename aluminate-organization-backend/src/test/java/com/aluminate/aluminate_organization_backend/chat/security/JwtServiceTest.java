package com.aluminate.aluminate_organization_backend.chat.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtServiceTest {

    private static final ObjectMapper M = new ObjectMapper();

    @Test
    void validTokenParses() throws Exception {
        String secret = "dev-secret-at-least-32-chars-long-1234567890";
        JwtService svc = new JwtService(secret);

        String token = makeToken(secret,
                Map.of("alg", "HS256", "typ", "JWT"),
                Map.of("sub", "user_1", "orgId", "org_123", "roles", new String[]{"ADMIN"}, "exp", Instant.now().getEpochSecond() + 3600));

        JwtPrincipal p = svc.validateAndExtract("Bearer " + token);
        assertEquals("user_1", p.getUserId());
        assertEquals("org_123", p.getOrgId());
        assertTrue(p.isAdmin());
    }

    @Test
    void invalidSignatureRejected() throws Exception {
        String secret = "dev-secret-at-least-32-chars-long-1234567890";
        JwtService svc = new JwtService(secret);

        String token = makeToken("wrong-secret",
                Map.of("alg", "HS256", "typ", "JWT"),
                Map.of("sub", "user_1", "orgId", "org_123", "roles", "MEMBER", "exp", Instant.now().getEpochSecond() + 3600));

        assertThrows(IllegalArgumentException.class, () -> svc.validateAndExtract("Bearer " + token));
    }

    private static String makeToken(String secret, Map<String, Object> header, Map<String, Object> payload) throws Exception {
        String h = b64(M.writeValueAsBytes(header));
        String p = b64(M.writeValueAsBytes(payload));
        String unsigned = h + "." + p;
        String sig = sign(unsigned, secret);
        return unsigned + "." + sig;
        }

    private static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
