package com.ewalletgateway.api.dto;

import java.math.BigDecimal;

/** JSON giống wallet-registry GET /api/v1/wallets/{id} */
public record WalletResponse(
        Long walletId,
        Long userId,
        String userName,
        BigDecimal balance,
        Long version
) {
}
