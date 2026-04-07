package com.app.ewallet.wsgateway.redis;

import com.app.ewallet.wsgateway.dto.TransferResultPayload;
import com.app.ewallet.wsgateway.websocket.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Nhận message từ Redis Pub/Sub (thay Kafka transfer.result) → push WebSocket theo {@code userId}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultRedisSubscriber {

    private final ObjectMapper objectMapper;
    private final WebSocketSessionRegistry sessionRegistry;

    /** Được gọi bởi {@link org.springframework.data.redis.listener.adapter.MessageListenerAdapter} */
    public void onMessage(String json) {
        long tRecv = System.nanoTime();
        int len = json != null ? json.length() : 0;
        log.debug("[ws-gateway] Redis message raw len={}", len);
        try {
            TransferResultPayload payload = objectMapper.readValue(json, TransferResultPayload.class);
            long tParsed = System.nanoTime();
            log.info(
                    "[ws-gateway] Redis SUBSCRIBE parsed userId={} requestId={} status={} txId={} parseNs={}",
                    payload.userId(),
                    payload.requestId(),
                    payload.status(),
                    payload.transactionId(),
                    tParsed - tRecv
            );
            sessionRegistry.broadcastToUser(payload.userId(), json);
            log.info(
                    "[ws-gateway] WS broadcast finished userId={} requestId={} totalNsSinceReceive={}",
                    payload.userId(),
                    payload.requestId(),
                    System.nanoTime() - tRecv
            );
        } catch (Exception e) {
            log.warn("Failed to handle transfer result from Redis: {}", e.getMessage(), e);
        }
    }
}
