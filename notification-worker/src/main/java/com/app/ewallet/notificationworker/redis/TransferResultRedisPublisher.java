package com.app.ewallet.notificationworker.redis;

import com.app.ewallet.notificationworker.config.properties.RedisPubSubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class TransferResultRedisPublisher implements TransferResultRealtimePublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisPubSubProperties redisPubSubProperties;

    @Override
    public void publish(String jsonPayload) {
        String channel = redisPubSubProperties.transferResultChannel();
        int len = jsonPayload != null ? jsonPayload.length() : 0;
        long t0 = System.nanoTime();
        try {
            stringRedisTemplate.convertAndSend(channel, jsonPayload);
            log.info(
                    "[transfer-result-redis] PUBLISH ok channel={} len={} convertSendNs={}",
                    channel,
                    len,
                    System.nanoTime() - t0
            );
        } catch (Exception e) {
            log.warn("Redis PUBLISH failed channel={} len={}", channel, len, e);
        }
    }
}
