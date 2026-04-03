package com.app.ewallet.wsgateway;

import com.app.ewallet.wsgateway.config.properties.CorsWsProperties;
import com.app.ewallet.wsgateway.config.properties.JwtProperties;
import com.app.ewallet.wsgateway.config.properties.KafkaTopicsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties({
        JwtProperties.class,
        KafkaTopicsProperties.class,
        CorsWsProperties.class
})
public class WsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsGatewayApplication.class, args);
    }
}
