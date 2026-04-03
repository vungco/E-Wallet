package com.app.ewallet.wsgateway.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Khớp JSON từ transfer-service outbox → topic {@code transfer.result}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferResultPayload(
        long userId,
        String requestId,
        String status,
        Long transactionId,
        String errorMessage
) {
}
