package com.app.ewallet.wsgateway.websocket;

import com.app.ewallet.wsgateway.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Bắt buộc query {@code ?token=<JWT>} (access token) — cùng secret/issuer với wallet-registry.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    static final String ATTR_USER_ID = "userId";

    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        URI uri = request.getURI();
        String token = extractToken(uri != null ? uri.getQuery() : null);
        if (token == null || token.isBlank()) {
            log.warn("WS handshake rejected: missing token query param");
            return false;
        }
        try {
            Claims claims = jwtService.parseAndValidate(token.trim());
            Long userId = jwtService.getUserId(claims);
            attributes.put(ATTR_USER_ID, userId);
            return true;
        } catch (Exception e) {
            log.warn("WS handshake rejected: invalid token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }

    static String extractToken(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String part : query.split("&")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = part.substring(0, eq);
            String value = part.substring(eq + 1);
            if ("token".equals(key)) {
                return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
