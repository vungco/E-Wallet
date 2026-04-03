package com.app.ewallet.wsgateway.config;

import com.app.ewallet.wsgateway.config.properties.CorsWsProperties;
import com.app.ewallet.wsgateway.websocket.JwtHandshakeInterceptor;
import com.app.ewallet.wsgateway.websocket.TransferWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TransferWebSocketHandler transferWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CorsWsProperties corsWsProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var registration = registry.addHandler(transferWebSocketHandler, "/ws")
                .addInterceptors(jwtHandshakeInterceptor);
        if (StringUtils.hasText(corsWsProperties.allowedOrigins())) {
            registration.setAllowedOrigins(corsWsProperties.allowedOrigins().split(","));
        } else {
            registration.setAllowedOriginPatterns("*");
        }
    }
}
