package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis.pubsub")
public record RedisPubSubProperties(
        /** Kênh Redis Pub/Sub — ws-gateway subscribe cùng tên */
        String transferResultChannel
) {
}
