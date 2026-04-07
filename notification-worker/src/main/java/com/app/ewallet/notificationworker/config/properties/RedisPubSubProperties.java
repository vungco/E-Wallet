package com.app.ewallet.notificationworker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.redis.pubsub")
public record RedisPubSubProperties(
        /** Cùng kênh Redis Pub/Sub với ws-gateway (subscribe → WebSocket). */
        String transferResultChannel
) {
}
