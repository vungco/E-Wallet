package com.app.ewallet.notificationworker.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

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
