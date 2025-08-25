package com.aluminate.aluminate_organization_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security.
 * This class defines the security filter chain and configures HTTP security settings.
 */
@Configuration
public class SecurityConfig {

    private final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain with API prefix: {}", apiPrefix);

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(apiPrefix + "/auth/**", apiPrefix + "/public/**").permitAll()

                        // Role-based endpoints - FIXED TO ACCEPT BOTH CASES
                        .requestMatchers(apiPrefix + "/admin/**").hasAnyRole("ADMIN", "admin")
                        .requestMatchers(apiPrefix + "/member/**").hasAnyRole("MEMBER", "member")
                        .requestMatchers(apiPrefix + "/common/**").hasAnyRole("ADMIN", "MEMBER", "admin", "member")

                        // Campaign endpoints - require ADMIN role
                        .requestMatchers(apiPrefix + "/campaign/**").hasAnyRole("ADMIN", "admin")

                        // User payment endpoints - require MEMBER role - FIXED
                        .requestMatchers(apiPrefix + "/user/**").hasAnyRole("MEMBER", "member")

                        // Deny all other requests
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}