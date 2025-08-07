package com.aluminate.aluminate_organization_backend.config;

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

        /**
         * Defines the security filter chain bean.
         * Configures HTTP security to allow all requests without authentication.
         *
         * @param http the HttpSecurity object used to configure security settings
         * @return the configured SecurityFilterChain
         * @throws Exception if an error occurs while building the security filter chain
         */

        @Value("${api.prefix}")
        private String apiPrefix;
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .cors(Customizer.withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(apiPrefix + "/auth/**", apiPrefix + "/public/**").permitAll()
                            .requestMatchers(apiPrefix + "/admin/**").hasRole("ADMIN")
                            .requestMatchers(apiPrefix + "/member/**").hasRole("MEMBER")
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