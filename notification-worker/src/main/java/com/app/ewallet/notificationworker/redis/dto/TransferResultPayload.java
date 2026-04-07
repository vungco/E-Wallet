package com.app.ewallet.notificationworker.redis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON gửi Redis Pub/Sub — cùng schema ws-gateway {@code TransferResultPayload}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferResultPayload(
        long userId,
        String requestId,
        String status,
        Long transactionId,
        String errorMessage
) {
}
