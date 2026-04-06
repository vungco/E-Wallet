package com.app.ewallet.config;

import com.app.ewallet.config.properties.KafkaTopicsProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class KafkaTopicConfig {

    @Bean
    NewTopic transferCommandTopic(KafkaTopicsProperties topics) {
        return TopicBuilder.name(topics.transferCommand()).partitions(6).replicas(1).build();
    }

    @Bean
    NewTopic walletTransferCompletedTopic(KafkaTopicsProperties topics) {
        return TopicBuilder.name(topics.walletTransferCompleted()).partitions(3).replicas(1).build();
    }
}
