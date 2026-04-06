package com.app.ewallet.wsgateway.config;

import com.app.ewallet.wsgateway.config.properties.RedisPubSubProperties;
import com.app.ewallet.wsgateway.redis.TransferResultRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Đăng ký subscriber sau khi {@link RedisConnectionFactory} có trong context (Data Redis auto-config).
 * Không dùng {@code @ConditionalOnBean} ở cấp class: có thể bị đánh giá trước khi auto-config tạo factory
 * → không có listener, trong khi PUBLISH vẫn thành công (0 subscriber).
 */
@Configuration
public class RedisPubSubConfig {

    /** Bean riêng để Spring gọi {@code afterPropertiesSet()} trên {@link MessageListenerAdapter} (khởi tạo invoker). */
    @Bean
    MessageListenerAdapter transferResultMessageListenerAdapter(TransferResultRedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter transferResultMessageListenerAdapter,
            RedisPubSubProperties redisPubSubProperties
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                transferResultMessageListenerAdapter,
                new ChannelTopic(redisPubSubProperties.transferResultChannel())
        );
        return container;
    }
}
