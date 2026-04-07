package com.app.ewallet.notificationworker.email;

import com.app.ewallet.notificationworker.config.properties.MailAppProperties;
import com.app.ewallet.notificationworker.model.Notification;
import com.app.ewallet.notificationworker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Gửi email qua SMTP (Gmail: dùng app password). Bật profile {@code smtp} + cấu hình {@code spring.mail.*}.
 * Địa chỉ nhận lấy từ {@link Notification#getRecipientEmail()} — đồng bộ từ payload Kafka.
 */
@Component
@Profile("smtp")
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailNotificationSender implements EmailNotificationSender {

    private final JavaMailSender mailSender;
    private final Environment environment;
    private final MailAppProperties mailAppProperties;
    private final NotificationRepository notificationRepository;

    @Override
    public void sendTransferNotification(Notification notification) {
        try {
            if (!StringUtils.hasText(notification.getRecipientEmail())) {
                log.warn(
                        "Skip email: recipientEmail empty on notification (Kafka payload thiếu fromEmail/toEmail) userId={}",
                        notification.getUserId()
                );
                return;
            }
            String to = notification.getRecipientEmail().trim();
            String from = resolveFrom();
            if (!StringUtils.hasText(from)) {
                log.warn("Skip email: spring.mail.username / app.mail.from not set");
                return;
            }

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(notification.getTitle());
            msg.setText(notification.getBody());

            mailSender.send(msg);

            notification.setEmailSent(true);
            notificationRepository.save(notification);
            log.info("Email sent notificationId={} toUserId={}", notification.getId(), notification.getUserId());
        } catch (Exception e) {
            log.error(
                    "Email send failed notificationId={} userId={}: {}",
                    notification.getId(),
                    notification.getUserId(),
                    e.getMessage()
            );
        }
    }

    private String resolveFrom() {
        if (StringUtils.hasText(mailAppProperties.from())) {
            return mailAppProperties.from().trim();
        }
        String u = environment.getProperty("spring.mail.username", "");
        return u != null ? u.trim() : "";
    }
}
