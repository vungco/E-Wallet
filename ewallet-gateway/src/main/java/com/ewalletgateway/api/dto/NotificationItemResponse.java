package com.ewalletgateway.api.dto;

public record NotificationItemResponse(
        long id,
        String title,
        String body,
        String transferStatus,
        boolean read,
        String requestId,
        Long transactionId,
        String amount,
        Long counterpartUserId,
        String userRole,
        String createdAt
) {
}
