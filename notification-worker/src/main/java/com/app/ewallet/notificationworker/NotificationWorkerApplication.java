package com.app.ewallet.notificationworker;

import com.app.ewallet.notificationworker.config.properties.GrpcServerProperties;
import com.app.ewallet.notificationworker.config.properties.JwtProperties;
import com.app.ewallet.notificationworker.config.properties.KafkaTopicsProperties;
import com.app.ewallet.notificationworker.config.properties.MailAppProperties;
import com.app.ewallet.notificationworker.config.properties.RedisPubSubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties({
        JwtProperties.class,
        GrpcServerProperties.class,
        KafkaTopicsProperties.class,
        MailAppProperties.class,
        RedisPubSubProperties.class
})
public class NotificationWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationWorkerApplication.class, args);
    }
}
