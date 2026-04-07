package com.app.ewallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON gửi topic {@code wallet.transfer.failed} — notification-worker đẩy Redis → ws-gateway.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletTransferFailedPayload(
        long transactionId,
        String requestId,
        long fromUserId,
        long toUserId,
        String errorMessage
) {
}
