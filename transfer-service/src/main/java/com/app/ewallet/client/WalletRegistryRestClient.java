package com.app.ewallet.client;

import com.app.ewallet.client.dto.WalletRegistryWalletDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class WalletRegistryRestClient {

    private final WebClient walletRegistryWebClient;

    public WalletRegistryWalletDto getWallet(String bearerAccessToken, long walletId) {
        try {
            return walletRegistryWebClient.get()
                    .uri("/api/v1/wallets/{id}", walletId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAccessToken)
                    .retrieve()
                    .bodyToMono(WalletRegistryWalletDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            int code = e.getStatusCode().value();
            if (code == 403 || code == 404) {
                return null;
            }
            throw e;
        }
    }
}
