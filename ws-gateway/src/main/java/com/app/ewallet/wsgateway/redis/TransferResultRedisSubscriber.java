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
        log.debug("Redis Pub/Sub raw message (len={}): {}", json != null ? json.length() : 0, json);
        try {
            TransferResultPayload payload = objectMapper.readValue(json, TransferResultPayload.class);
            log.info(
                    "Redis transfer result received: userId={} requestId={} status={} transactionId={}",
                    payload.userId(),
                    payload.requestId(),
                    payload.status(),
                    payload.transactionId()
            );
            sessionRegistry.broadcastToUser(payload.userId(), json);
            log.info(
                    "Redis transfer result pushed to WebSocket: userId={} requestId={}",
                    payload.userId(),
                    payload.requestId()
            );
        } catch (Exception e) {
            log.warn("Failed to handle transfer result from Redis: {}", e.getMessage(), e);
        }
    }
}
