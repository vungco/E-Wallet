package com.app.ewallet.config;

import com.app.ewallet.config.properties.WalletRegistryRestProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient walletRegistryWebClient(WalletRegistryRestProperties props) {
        String base = props.baseUrl().replaceAll("/+$", "");
        return WebClient.builder().baseUrl(base).build();
    }
}
