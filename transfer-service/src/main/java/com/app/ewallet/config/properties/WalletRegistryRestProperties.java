package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.wallet-registry")
public record WalletRegistryRestProperties(
        String baseUrl
) {
}
