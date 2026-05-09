package com.dammroute.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key authentication filter.
 *
 * Clients must send header: X-API-Key: <key>
 *
 * Security note: in production, rotate this key regularly and
 * store it in a secrets manager (AWS Secrets Manager, HashiCorp Vault).
 * Never commit real API keys to Git.
 */
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final String validApiKey;

    public ApiKeyAuthFilter(String validApiKey) {
        if (validApiKey == null || validApiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be null or blank");
        }
        this.validApiKey = validApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter for public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/h2-console") || path.equals("/api/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);

        if (providedKey == null || !providedKey.equals(validApiKey)) {
            // Security: never reveal why auth failed (don't say "wrong key")
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"Valid API key required\"}"
            );
            return;
        }

        // Valid key — set authentication in context
        var auth = new UsernamePasswordAuthenticationToken(
            "api-client",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_API"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
