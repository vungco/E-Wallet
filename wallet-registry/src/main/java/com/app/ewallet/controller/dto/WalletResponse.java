package com.app.ewallet.controller.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long walletId,
        Long userId,
        String userName,
        BigDecimal balance,
        Long version
) {
}
