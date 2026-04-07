package com.app.ewallet.notificationworker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        int accessTokenExpirationMinutes,
        int refreshTokenExpirationDays
) {
}
