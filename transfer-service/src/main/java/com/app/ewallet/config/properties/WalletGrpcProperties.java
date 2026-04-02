package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.grpc.wallet")
public record WalletGrpcProperties(
        String host,
        int port,
        String internalApiKey
) {
}
