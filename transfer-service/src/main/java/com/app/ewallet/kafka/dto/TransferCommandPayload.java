package com.app.ewallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferCommandPayload(
        String requestId,
        long fromWalletId,
        long toWalletId,
        long fromUserId,
        long toUserId,
        String amount
) {
}
