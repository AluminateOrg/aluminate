package com.aluminate.aluminate_organization_backend.config;

import com.aluminate.aluminate_organization_backend.config.util.Jwt;

import com.aluminate.aluminate_organization_backend.model.*;
import com.aluminate.aluminate_organization_backend.repository.OrganizationSettingsRepository;
import com.aluminate.aluminate_organization_backend.service.CustomUserDetailsService;
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
    private final CustomUserDetailsService customUserDetailsService;



    @Value("${api.prefix}")
    private String apiPrefix;

    public JwtAuthenticationFilter(Jwt jwtUtil,
                                   CsrfTokenService csrfTokenService,
                                   OrganizationSettingsRepository organizationSettingsRepository,
                                      CustomUserDetailsService customUserDetailsService


    ) {
        this.jwtUtil = jwtUtil;
        this.csrfTokenService = csrfTokenService;
        this.organizationSettingsRepository = organizationSettingsRepository;
        this.customUserDetailsService = customUserDetailsService;


    }

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("JwtAuthenticationFilter called for path: " + path);

        String finalPrefix = "/" + apiPrefix;
        String cleanPath = path.trim();
        if (!cleanPath.startsWith(finalPrefix + "/admin/") &&
                !cleanPath.startsWith(finalPrefix + "/member/") &&
                !cleanPath.startsWith(finalPrefix + "/common/")) {
            logger.info("Skipping JWT authentication for non-admin/member path: " + cleanPath);
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

            String email = claims.get("email", String.class);
            //check if email is null
            if (email == null) {
                unauthorized(response, "Invalid token claims");
                logger.error("Invalid token claims: email is null");
                return;
            }
            //check if user is a member or admin
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            if (userDetails == null) {
                unauthorized(response, "Invalid user email");
                logger.error("Invalid user email: " + email);
                return;
            }
            //get role
            if((userDetails instanceof Admin)){
                Admin admin = (Admin)  userDetails;
                //get organization
                Organization organization = (Organization) admin.getOrganization();
                //check if organization is null or status is not ACTIVE
                if (organization == null || organization.getStatus() != Status.ACTIVE) {
                    unauthorized(response, "Organization is not active or does not exist");
                    logger.error("Organization is not active or does not exist for admin: " + admin.getEmail());
                    return;
                }
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);

                filterChain.doFilter(request, response);
                logger.info("Successfully passed JWT authentication for admin: " + admin.getEmail());
            }
            else if((userDetails instanceof Member)){
                //check whether the membership-fee is free or not
                Member member = (Member) userDetails;
                //get org
                Organization organization = (Organization) member.getOrganization();

                //check if organization is null or status is not ACTIVE
                if (organization == null || organization.getStatus() != Status.ACTIVE) {
                    unauthorized(response, "Organization is not active or does not exist");
                    logger.error("Organization is not active or does not exist for member: " + member.getEmail());
                    return;
                }

                if (requiresMembershipFee(path) && !organization.isMembershipFree()) {
                    //reject
                    unauthorized(response, "Membership fee required for this route");
                    logger.error("Membership fee is not paid");
                    return;
                }else{
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    filterChain.doFilter(request, response);
                    logger.info("Successfully passed JWT authentication for member: " + member.getEmail());
                }
            }






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
        String finalPrefix = "/" + apiPrefix;
         return uri.startsWith(finalPrefix + "/member/");

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
