package com.aluminate.aluminate_organization_backend.chat.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Minimal HS256 JWT validator without external dependencies.
 * Expects claims to include at least: sub (userId), orgId, roles (array or comma-separated string), exp (optional).
 */
@Service
public class JwtService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final byte[] secret;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public JwtPrincipal validateAndExtract(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = bearerToken.substring("Bearer ".length());
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT structure");
        }

        String headerB64 = parts[0];
        String payloadB64 = parts[1];
        String signatureB64 = parts[2];

        Map<String, Object> headerJson = parseJson(base64UrlDecodeToString(headerB64));
        String alg = String.valueOf(headerJson.getOrDefault("alg", "HS256"));

        String signingInput = headerB64 + "." + payloadB64;
        String computedSig = sign(signingInput, alg);

        if (!constantTimeEquals(signatureB64, computedSig)) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        Map<String, Object> claims = parseJson(base64UrlDecodeToString(payloadB64));
        // exp check
        Object expObj = claims.get("exp");
        if (expObj != null) {
            long exp = (expObj instanceof Number) ? ((Number) expObj).longValue()
                    : Long.parseLong(expObj.toString());
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("JWT expired");
            }
        }

        String userId = optionalStringClaim(claims, "userId");
        if (userId == null) userId = stringClaim(claims, "sub");
//        String userId = stringClaim(claims, "sub");
        String orgId = stringClaim(claims, "orgId");
        Set<String> roles = extractRoles(claims.get("roles"));
        return JwtPrincipal.of(userId, orgId, roles);
    }

    private String optionalStringClaim(Map<String, Object> claims, String name) {
        Object v = claims.get(name);
        return v == null ? null : String.valueOf(v);
    }


    private String stringClaim(Map<String, Object> claims, String name) {
        Object val = claims.get(name);
        if (val == null) throw new IllegalArgumentException("Missing claim: " + name);
        return val.toString();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Object rolesObj) {
        if (rolesObj == null) return new HashSet<>();
        if (rolesObj instanceof Iterable) {
            Set<String> out = new HashSet<>();
            for (Object r : (Iterable<Object>) rolesObj) {
                out.add(String.valueOf(r));
            }
            return out;
        }
        String s = rolesObj.toString();
        Set<String> out = new HashSet<>();
        for (String r : s.split(",")) {
            String t = r.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private String sign(String data, String alg) {
        try {
            String jcaAlg = switch (alg) {
                case "HS512" -> "HmacSHA512";
                case "HS384" -> "HmacSHA384";
                default -> "HmacSHA256";
            };
            Mac mac = Mac.getInstance(jcaAlg);
            mac.init(new SecretKeySpec(secret, jcaAlg));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    private String base64UrlDecodeToString(String b64) {
        return new String(Base64.getUrlDecoder().decode(b64), StandardCharsets.UTF_8);
    }

    private Map<String, Object> parseJson(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT JSON", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int res = 0;
        for (int i = 0; i < a.length(); i++) {
            res |= a.charAt(i) ^ b.charAt(i);
        }
        return res == 0;
    }
}
