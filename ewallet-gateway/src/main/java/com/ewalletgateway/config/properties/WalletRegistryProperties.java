package com.ewalletgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.wallet-registry")
public record WalletRegistryProperties(
        String host,
        int port,
        String internalApiKey
) {
}
