package com.ewalletgateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificationGrpcProperties.class)
public class NotificationGrpcChannelConfig {

    @Bean(destroyMethod = "shutdown")
    ManagedChannel notificationGrpcChannel(NotificationGrpcProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.host(), properties.port())
                .usePlaintext()
                .build();
    }
}
