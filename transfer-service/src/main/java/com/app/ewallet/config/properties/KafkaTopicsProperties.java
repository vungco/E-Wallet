package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String transferCommand,
        String walletTransferCompleted,
        String walletTransferFailed
) {
}
