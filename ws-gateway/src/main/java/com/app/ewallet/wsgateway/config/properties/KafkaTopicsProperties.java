package com.app.ewallet.wsgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(
        String transferResult
) {
}
