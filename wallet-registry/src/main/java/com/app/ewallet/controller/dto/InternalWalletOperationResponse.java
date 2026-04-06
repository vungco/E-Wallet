package com.app.ewallet.controller.dto;

import java.math.BigDecimal;

public record InternalWalletOperationResponse(
        Long walletId,
        BigDecimal balanceAfter,
        Long version,
        boolean replayed
) {
}
