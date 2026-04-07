package com.app.ewallet;

import com.app.ewallet.config.properties.GrpcServerProperties;
import com.app.ewallet.config.properties.JwtProperties;
import com.app.ewallet.config.properties.KafkaTopicsProperties;
import com.app.ewallet.config.properties.WalletGrpcProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
@EnableConfigurationProperties({
        JwtProperties.class,
        GrpcServerProperties.class,
        WalletGrpcProperties.class,
        KafkaTopicsProperties.class
})
public class TransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
