package com.app.ewallet.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String internalApiKey,
        boolean requireInternalKey
) {
}
