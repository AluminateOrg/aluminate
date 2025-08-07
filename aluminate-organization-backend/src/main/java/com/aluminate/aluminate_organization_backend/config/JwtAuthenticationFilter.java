package com.aluminate.aluminate_organization_backend.config;

import com.aluminate.aluminate_organization_backend.config.util.Jwt;

import com.aluminate.aluminate_organization_backend.model.OrganizationSettings;
import com.aluminate.aluminate_organization_backend.repository.OrganizationSettingsRepository;
import com.aluminate.aluminate_organization_backend.service.csrf.CsrfTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Jwt jwtUtil;
    private final CsrfTokenService csrfTokenService;
    private final OrganizationSettingsRepository organizationSettingsRepository;



    @Value("${api.prefix}")
    private String apiPrefix;

    public JwtAuthenticationFilter(Jwt jwtUtil,
                                   CsrfTokenService csrfTokenService,
                                   OrganizationSettingsRepository organizationSettingsRepository


    ) {
        this.jwtUtil = jwtUtil;
        this.csrfTokenService = csrfTokenService;
        this.organizationSettingsRepository = organizationSettingsRepository;


    }

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("JwtAuthenticationFilter called for path: " + path);


        if (!path.startsWith(apiPrefix + "/admin/") && !path.startsWith(apiPrefix + "/member/")) {
            logger.info("Skipping JWT authentication for non-admin/member path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("Checking authentication for admin/member secure path: " + path);

        // Validate CSRF token for state-changing methods (POST, PUT, DELETE, PATCH)
        if (requiresCsrfValidation(request)) {
            String csrfTokenFromHeader = request.getHeader("X-Csrf-Token");

            String sessionId = extractSessionIdFromRequest(request); // You decide how to get sessionId, maybe cookie or header

            if (csrfTokenFromHeader == null || sessionId == null || !csrfTokenService.validateToken(sessionId, csrfTokenFromHeader)) {
                logger.error("Invalid csrf token or session id invalid");
                forbidden(response, "Invalid or missing CSRF token");
                return;
            }
        }

        // JWT validation
        String jwt = extractJwtFromCookie(request);

        if (jwt == null) {
            logger.error("No JWT found in request");
            unauthorized(response, "Missing JWT token");
            return;
        }

        //jwt validation handling

        try {
            Claims claims = jwtUtil.extractAllClaims(jwt);

            String adminEmail = claims.get("adminEmail", String.class);
            //check if adminEmail is null




        } catch (Exception e) {
            logger.error(e.getMessage());
            unauthorized(response, "Invalid or expired JWT token");
        }
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean requiresMembershipFee(String uri) {
        // Customize which routes require active package
        // Example: only require active paid membership on sensitive routes
        if(uri.startsWith(apiPrefix + "/member/")) {
            //get organization settings


        }
        return true;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    private void forbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
    private boolean requiresCsrfValidation(HttpServletRequest request) {
        // Usually only for state-changing HTTP methods
        String method = request.getMethod();
        return method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE");
    }

    private String extractSessionIdFromRequest(HttpServletRequest request) {

        //  from header:
        return request.getHeader("X-Session-Id");

    }
}
