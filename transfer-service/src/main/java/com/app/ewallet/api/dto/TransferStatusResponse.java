package com.app.ewallet.api.dto;

import java.math.BigDecimal;

public record TransferStatusResponse(
        String requestId,
        String status,
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        String errorMessage
) {
}
