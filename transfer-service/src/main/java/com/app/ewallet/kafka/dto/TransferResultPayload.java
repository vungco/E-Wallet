package com.app.ewallet.kafka.dto;

public record TransferResultPayload(
        long userId,
        String requestId,
        String status,
        Long transactionId,
        String errorMessage
) {
}
