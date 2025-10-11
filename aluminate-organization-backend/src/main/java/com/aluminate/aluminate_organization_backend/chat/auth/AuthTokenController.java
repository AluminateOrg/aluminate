package com.aluminate.aluminate_organization_backend.chat.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.aluminate.aluminate_organization_backend.service.csrf.CsrfTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class AuthTokenController {

    private final CsrfTokenService csrfTokenService;

    @GetMapping("/auth/jwt")
    public ResponseEntity<?> getJwt(
            HttpServletRequest request,
            @RequestHeader(name = "X-Csrf-Token", required = false) String csrfToken,
            @RequestHeader(name = "X-Session-Id", required = false) String sessionId) {

        // Validate CSRF/session to prevent CSRF-based token exfiltration
        if (!StringUtils.hasText(csrfToken) || !StringUtils.hasText(sessionId)
                || !csrfTokenService.validateToken(sessionId, csrfToken)) {
            return ResponseEntity.status(403).body(Map.of("error", "Invalid or missing CSRF/Session"));
        }

        // Find the httpOnly JWT cookie
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("jwt".equals(c.getName())) {
                    jwt = c.getValue();
                    break;
                }
            }
        }

        if (!StringUtils.hasText(jwt)) {
            return ResponseEntity.status(401).body(Map.of("error", "JWT not found"));
        }

        // Return the raw token so the frontend can use it for Authorization headers
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}