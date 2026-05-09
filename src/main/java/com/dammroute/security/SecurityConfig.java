package com.dammroute.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.api-key}")
    private String apiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless REST API uses API key, not sessions
            .csrf(AbstractHttpConfigurer::disable)

            // CORS — allow React frontend on localhost:3000
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless — no sessions, every request must carry API key
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Request authorization rules
            .authorizeHttpRequests(auth -> auth
                // H2 console — dev only, never enable in production
                .requestMatchers("/h2-console/**").permitAll()
                // Public health check
                .requestMatchers("/api/health").permitAll()
                // All other API endpoints require API key
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )

            // Allow H2 console frames — dev only
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()))

            // Add API key filter before Spring's auth filter
            .addFilterBefore(
                new ApiKeyAuthFilter(apiKey),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow our React frontend — never use * in production
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173"  // Vite default port
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
