package com.app.ewallet.redis;

/**
 * Pub JSON kết quả chuyển tiền (Redis) sau khi DB commit.
 */
public interface TransferResultRealtimePublisher {

    void publish(String jsonPayload);
}
