package com.app.ewallet.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WalletRegistryWalletDto(
        Long walletId,
        Long userId,
        String userName,
        BigDecimal balance,
        Long version
) {
}
