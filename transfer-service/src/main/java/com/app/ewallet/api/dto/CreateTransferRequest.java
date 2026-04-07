package com.app.ewallet.api.dto;

import java.math.BigDecimal;

public record CreateTransferRequest(
        String requestId,
        Long fromWalletId,
        Long toWalletId,
        Long toUserId,
        BigDecimal amount,
        String fromUserEmail,
        String toUserEmail
) {
}
