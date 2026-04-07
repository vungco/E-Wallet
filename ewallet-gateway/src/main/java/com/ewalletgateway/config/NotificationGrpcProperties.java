package com.ewalletgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.grpc")
public record NotificationGrpcProperties(
        String host,
        int port
) {
}
