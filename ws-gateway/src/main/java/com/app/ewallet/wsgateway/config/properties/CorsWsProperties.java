package com.app.ewallet.wsgateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ws.cors")
public record CorsWsProperties(
        /** Danh sách origin cách nhau bởi dấu phẩy; để trống → cho phép mọi origin (pattern). */
        String allowedOrigins
) {
}
