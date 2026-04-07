package com.app.ewallet.notificationworker.kafka;

import com.app.ewallet.notificationworker.kafka.dto.WalletTransferFailedPayload;
import com.app.ewallet.notificationworker.redis.TransferResultRealtimePublisher;
import com.app.ewallet.notificationworker.redis.dto.TransferResultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletTransferFailedKafkaListener {

    private final ObjectMapper objectMapper;
    private final TransferResultRealtimePublisher transferResultRealtimePublisher;

    @KafkaListener(
            topics = "${app.kafka.topics.wallet-transfer-failed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onWalletTransferFailed(String json) {
        try {
            WalletTransferFailedPayload p = objectMapper.readValue(json, WalletTransferFailedPayload.class);
            String fromJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    p.fromUserId(),
                    p.requestId(),
                    "FAILED",
                    p.transactionId(),
                    p.errorMessage()
            ));
            String toJson = objectMapper.writeValueAsString(new TransferResultPayload(
                    p.toUserId(),
                    p.requestId(),
                    "FAILED",
                    p.transactionId(),
                    p.errorMessage()
            ));
            transferResultRealtimePublisher.publish(fromJson);
            transferResultRealtimePublisher.publish(toJson);
        } catch (Exception e) {
            log.warn("Failed to process wallet.transfer.failed for Redis: {}", e.getMessage());
        }
    }
}
