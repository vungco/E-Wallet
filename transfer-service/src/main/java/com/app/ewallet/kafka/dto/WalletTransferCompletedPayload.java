package com.app.ewallet.kafka.dto;

import java.math.BigDecimal;

public record WalletTransferCompletedPayload(
        long transactionId,
        String requestId,
        BigDecimal amount,
        long fromUserId,
        long toUserId,
        String occurredAt
) {
}
