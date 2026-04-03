package com.app.ewallet.wsgateway.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry registry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId == null) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing userId"));
            } catch (Exception ignored) {
            }
            return;
        }
        registry.register(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Optional: ping/pong — hiện không bắt buộc; client có thể chỉ nhận push.
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId != null) {
            registry.unregister(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        if (userId != null) {
            registry.unregister(userId, session);
        }
    }
}
