package com.app.ewallet.notificationworker.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Địa chỉ From (tuỳ chọn — mặc định dùng {@code spring.mail.username}, Gmail thường trùng).
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailAppProperties(
        String from
) {
}
