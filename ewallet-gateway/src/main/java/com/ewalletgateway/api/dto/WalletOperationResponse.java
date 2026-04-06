package com.ewalletgateway.api.dto;

import java.math.BigDecimal;

public record WalletOperationResponse(
        Long walletId,
        BigDecimal balanceAfter,
        Long version,
        boolean replayed
) {
}
