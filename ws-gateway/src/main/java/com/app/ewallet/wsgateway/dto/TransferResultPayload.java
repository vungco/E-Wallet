package com.app.ewallet.wsgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON từ transfer-service (Redis Pub/Sub) — cùng schema trước đây gửi Kafka.
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
