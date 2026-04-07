package com.app.ewallet.wsgateway.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Map userId ↔ nhiều WebSocket session (nhiều tab / thiết bị).
 */
@Component
@Slf4j
public class WebSocketSessionRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("WS registered userId={} sessionId={}", userId, session.getId());
    }

    public void unregister(Long userId, WebSocketSession session) {
        CopyOnWriteArraySet<WebSocketSession> set = userSessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userSessions.remove(userId, set);
            }
        }
        log.debug("WS unregistered userId={} sessionId={}", userId, session.getId());
    }

    public void broadcastToUser(long userId, String jsonPayload) {
        CopyOnWriteArraySet<WebSocketSession> set = userSessions.get(userId);
        if (set == null || set.isEmpty()) {
            log.warn(
                    "[ws-gateway] WS skip push: no open session for userId={} (client chưa mở /ws hoặc token khác tab). payloadLen={}",
                    userId,
                    jsonPayload != null ? jsonPayload.length() : 0
            );
            return;
        }
        int openCount = 0;
        long t0 = System.nanoTime();
        TextMessage message = new TextMessage(jsonPayload);
        for (WebSocketSession session : set) {
            if (!session.isOpen()) {
                set.remove(session);
                continue;
            }
            openCount++;
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.warn("WS send failed userId={} sessionId={}", userId, session.getId(), e);
                set.remove(session);
            }
        }
        log.info(
                "[ws-gateway] WS sent userId={} sessions={} payloadLen={} sendNs={}",
                userId,
                openCount,
                jsonPayload != null ? jsonPayload.length() : 0,
                System.nanoTime() - t0
        );
    }
}
