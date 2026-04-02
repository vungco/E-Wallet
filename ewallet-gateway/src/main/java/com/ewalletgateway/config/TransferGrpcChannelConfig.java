package com.ewalletgateway.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TransferGrpcProperties.class)
public class TransferGrpcChannelConfig {

    @Bean(destroyMethod = "shutdown")
    ManagedChannel transferGrpcChannel(TransferGrpcProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.host(), properties.port())
                .usePlaintext()
                .build();
    }
}
