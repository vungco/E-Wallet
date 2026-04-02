package com.app.ewallet.config;

import com.app.ewallet.config.properties.WalletGrpcProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Bean(destroyMethod = "shutdown")
    ManagedChannel walletRegistryGrpcChannel(WalletGrpcProperties props) {
        return ManagedChannelBuilder.forAddress(props.host(), props.port())
                .usePlaintext()
                .build();
    }
}
