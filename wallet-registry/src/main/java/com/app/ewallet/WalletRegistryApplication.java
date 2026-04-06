package com.app.ewallet;

import com.app.ewallet.config.properties.GrpcServerProperties;
import com.app.ewallet.config.properties.JwtProperties;
import com.app.ewallet.config.properties.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class, GrpcServerProperties.class})
public class WalletRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletRegistryApplication.class, args);
    }
}
