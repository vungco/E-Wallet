package com.ewalletgateway.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lấy header {@code Authorization: Bearer …} đã được {@link JwtAuthenticationFilter} gắn vào
 * {@link Authentication#getCredentials()} — tránh lặp {@code @RequestHeader} trên controller.
 */
public final class AuthorizationHeaderAccessor {

    private AuthorizationHeaderAccessor() {
    }

    public static String bearerHeader(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Object creds = authentication.getCredentials();
        if (creds instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization");
    }
}
