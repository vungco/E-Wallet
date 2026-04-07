package com.app.ewallet.notificationworker.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Khớp JSON từ transfer-service outbox → topic {@code wallet.transfer.completed}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WalletTransferCompletedPayload(
        long transactionId,
        String requestId,
        BigDecimal amount,
        long fromUserId,
        long toUserId,
        @JsonAlias("occurredAt")
        String timestamp,
        @JsonAlias("fromUserEmail")
        String fromEmail,
        @JsonAlias("toUserEmail")
        String toEmail,
        BigDecimal fromBalanceAfter,
        BigDecimal toBalanceAfter
) {
}
