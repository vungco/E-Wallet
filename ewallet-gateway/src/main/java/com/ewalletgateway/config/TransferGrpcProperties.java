package com.ewalletgateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.transfer.grpc")
public record TransferGrpcProperties(
        String host,
        int port,
        /** Gửi metadata {@code x-internal-api-key}; cùng chuỗi với wallet-registry khi dùng chung env. */
        String internalApiKey
) {
}
