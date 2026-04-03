package com.app.ewallet.wsgateway.kafka;

import com.app.ewallet.wsgateway.kafka.dto.TransferResultPayload;
import com.app.ewallet.wsgateway.websocket.WebSocketSessionRegistry;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume {@code transfer.result} từ transfer-service (outbox) → push tới WebSocket theo {@code userId}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultKafkaListener {

    private final ObjectMapper objectMapper;
    private final WebSocketSessionRegistry sessionRegistry;

    @KafkaListener(
            topics = "${app.kafka.topics.transfer-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onTransferResult(String json) {
        try {
            TransferResultPayload payload = objectMapper.readValue(json, TransferResultPayload.class);
            sessionRegistry.broadcastToUser(payload.userId(), json);
        } catch (Exception e) {
            log.warn("Failed to handle transfer.result message: {}", e.getMessage());
        }
    }
}
