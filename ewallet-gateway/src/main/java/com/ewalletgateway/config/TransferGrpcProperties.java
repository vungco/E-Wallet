package com.ewalletgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.transfer.grpc")
public record TransferGrpcProperties(
        String host,
        int port
) {
}
