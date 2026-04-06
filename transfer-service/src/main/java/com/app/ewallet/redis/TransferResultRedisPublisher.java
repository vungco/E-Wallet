package com.app.ewallet.redis;

import com.app.ewallet.config.properties.RedisPubSubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Đẩy kết quả chuyển tiền real-time qua Redis Pub/Sub (thay Kafka topic transfer.result — độ trễ thấp hơn).
 */
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
        try {
            stringRedisTemplate.convertAndSend(channel, jsonPayload);
            log.warn("Redis pub success channel={} len={}", channel, jsonPayload != null ? jsonPayload.length() : 0);

        } catch (Exception e) {
            log.warn("Redis pub failed channel={} len={}", channel, jsonPayload != null ? jsonPayload.length() : 0, e);
        }
    }
}
