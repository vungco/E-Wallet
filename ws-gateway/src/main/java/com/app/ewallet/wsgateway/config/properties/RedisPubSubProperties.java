package com.app.ewallet.wsgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis.pubsub")
public record RedisPubSubProperties(
        /** Trùng transfer-service — subscribe kênh này */
        String transferResultChannel
) {
}
