package com.app.ewallet.wsgateway;

import com.app.ewallet.wsgateway.config.properties.CorsWsProperties;
import com.app.ewallet.wsgateway.config.properties.JwtProperties;
import com.app.ewallet.wsgateway.config.properties.RedisPubSubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        JwtProperties.class,
        RedisPubSubProperties.class,
        CorsWsProperties.class
})
public class WsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsGatewayApplication.class, args);
    }
}
