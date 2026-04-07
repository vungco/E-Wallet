package com.app.ewallet.notificationworker.kafka;

import com.app.ewallet.notificationworker.kafka.dto.WalletTransferCompletedPayload;
import com.app.ewallet.notificationworker.service.TransferCompletedNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletTransferCompletedKafkaListener {

    private final ObjectMapper objectMapper;
    private final TransferCompletedNotificationService notificationService;

    @KafkaListener(
            topics = "${app.kafka.topics.wallet-transfer-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onWalletTransferCompleted(String json) {
        try {
            WalletTransferCompletedPayload payload = objectMapper.readValue(json, WalletTransferCompletedPayload.class);
            notificationService.createNotificationsIfAbsent(payload);
        } catch (Exception e) {
            log.warn("Failed to process wallet.transfer.completed for notifications: {}", e.getMessage());
        }
    }
}
