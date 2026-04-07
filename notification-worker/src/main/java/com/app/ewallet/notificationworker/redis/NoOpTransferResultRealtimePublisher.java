package com.app.ewallet.notificationworker.redis;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpTransferResultRealtimePublisher implements TransferResultRealtimePublisher {

    @Override
    public void publish(String jsonPayload) {
        // no-op
    }
}
