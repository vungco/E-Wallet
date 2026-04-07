package com.app.ewallet.notificationworker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String walletTransferCompleted,
        String walletTransferFailed
) {
}
