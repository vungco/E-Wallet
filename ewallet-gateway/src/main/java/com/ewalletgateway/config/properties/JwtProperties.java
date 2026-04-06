package com.ewalletgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trùng {@code JWT_SECRET} / {@code JWT_ISSUER} với wallet-registry (verify access token).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String issuer
) {
}
