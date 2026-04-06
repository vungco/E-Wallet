package com.ewalletgateway.config;

import com.ewalletgateway.config.properties.WalletRegistryProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WalletRegistryProperties.class)
public class WalletRegistryGrpcChannelConfig {

    @Bean(destroyMethod = "shutdown")
    ManagedChannel walletRegistryGrpcChannel(WalletRegistryProperties properties) {
        return ManagedChannelBuilder.forAddress(properties.host(), properties.port())
                .usePlaintext()
                .build();
    }
}
