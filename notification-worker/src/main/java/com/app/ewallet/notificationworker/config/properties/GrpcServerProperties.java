package com.app.ewallet.notificationworker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.grpc.server")
public record GrpcServerProperties(
        boolean enabled,
        int port,
        boolean reflectionEnabled,
        int maxInboundMessageSizeBytes
) {
}
