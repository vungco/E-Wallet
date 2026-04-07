package com.app.ewallet.notificationworker.email;

import com.app.ewallet.notificationworker.model.Notification;

/**
 * Gửi email sau khi lưu bản ghi — triển khai thật (SMTP) có thể bật sau.
 */
public interface EmailNotificationSender {

    /**
     * Stub: log; production: gửi mail và cập nhật {@code emailSent} trên entity.
     */
    void sendTransferNotification(Notification notification);
}
