package com.aluminate.aluminate_organization_backend.config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.Customizer;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;

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
         * @param httpSecurity the HttpSecurity object used to configure security settings
         * @return the configured SecurityFilterChain
         * @throws Exception if an error occurs while building the security filter chain
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
            return httpSecurity
                    .csrf(csrf -> csrf.disable()) // Disable CSRF
                    .authorizeHttpRequests(authorize -> authorize
                            .anyRequest().permitAll()
                    )
                    .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

    }