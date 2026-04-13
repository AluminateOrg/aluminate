package com.aluminate.aluminate_organization_backend.config;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.filter.CorsFilter;

    import java.util.List;

    /**
     * Configuration class for setting up global CORS (Cross-Origin Resource Sharing) settings.
     * This class defines a bean to configure and enable CORS for the application.
     */
    @Configuration
    public class CorsGlobalConfig {

        @Value("${FRONTEND_URL}")
        private String frontendUrl;

        @Bean
        public CorsFilter corsFilter() {

            CorsConfiguration config = new CorsConfiguration();

            config.setAllowedOrigins(List.of(frontendUrl));

            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

            config.setAllowedHeaders(List.of(
                    "Authorization",
                    "Content-Type",
                    "X-CSRF-TOKEN",
                    "X-Requested-With",
                    "Accept",
                    "X-Session-Id"
            ));

            config.setExposedHeaders(List.of("Set-Cookie", "X-CSRF-TOKEN"));

            config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);

            System.out.println("CORS allowed origin: " + frontendUrl);

            return new CorsFilter(source);
        }
    }