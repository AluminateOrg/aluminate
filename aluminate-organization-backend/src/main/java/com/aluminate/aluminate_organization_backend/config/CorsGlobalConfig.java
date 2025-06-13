package com.aluminate.aluminate_organization_backend.config;

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

        /**
         * Creates and configures a CORS filter bean.
         * This filter allows cross-origin requests from specified origins with specific methods and headers.
         *
         * @return a CorsFilter instance configured with the specified CORS settings
         */
        @Bean
        public CorsFilter corsFilter() {
            CorsConfiguration config = new CorsConfiguration();

            // Specifies the allowed origins for cross-origin requests
            config.setAllowedOrigins(List.of("http://localhost:3000"));

            // Specifies the allowed HTTP methods for cross-origin requests
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

            // Specifies the allowed headers for cross-origin requests
            config.setAllowedHeaders(List.of("*"));

            // Allows credentials (e.g., cookies) to be included in cross-origin requests
            config.setAllowCredentials(true);

            // Registers the CORS configuration for all paths
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);

            return new CorsFilter(source);
        }
    }