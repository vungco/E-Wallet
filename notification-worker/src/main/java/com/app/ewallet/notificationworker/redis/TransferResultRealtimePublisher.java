package com.app.ewallet.notificationworker.redis;

/**
 * Pub JSON kết quả chuyển tiền (Redis) sau khi xử lý Kafka + DB/email.
 */
public interface TransferResultRealtimePublisher {

    void publish(String jsonPayload);
}
