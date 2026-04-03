package com.app.ewallet.auditworker.kafka.dto;

import java.math.BigDecimal;

/**
 * Khớp JSON từ transfer-service outbox → topic {@code wallet.transfer.completed}.
 */
public record WalletTransferCompletedPayload(
        long transactionId,
        String requestId,
        BigDecimal amount,
        long fromUserId,
        long toUserId,
        String occurredAt
) {
}
