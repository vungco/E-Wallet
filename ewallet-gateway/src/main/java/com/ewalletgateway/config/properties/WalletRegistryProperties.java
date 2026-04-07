package com.ewalletgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nạp từ {@code app.wallet-registry.*}. Key gRPC gửi đi: metadata {@code x-internal-api-key}.
 * Env: {@code WALLET_INTERNAL_API_KEY} hoặc fallback {@code INTERNAL_API_KEY} (xem application.yaml).
 */
@ConfigurationProperties(prefix = "app.wallet-registry")
public record WalletRegistryProperties(
        String host,
        int port,
        String internalApiKey
) {
}
