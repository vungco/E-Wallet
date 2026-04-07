package com.app.ewallet.notificationworker.email;

import com.app.ewallet.notificationworker.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mặc định khi không bật profile {@code smtp}: không gửi SMTP — chỉ log.
 * Gửi thật: {@code SPRING_PROFILES_ACTIVE} thêm {@code smtp} + env SMTP_* (email nhận từ payload Kafka).
 */
@Component
@Profile("!smtp")
@Slf4j
public class LoggingEmailNotificationSender implements EmailNotificationSender {

    @Override
    public void sendTransferNotification(Notification notification) {
        log.info(
                "[email stub] userId={} title={} requestId={} — chưa gửi SMTP (notification id={})",
                notification.getUserId(),
                notification.getTitle(),
                notification.getRequestId(),
                notification.getId()
        );
    }
}
