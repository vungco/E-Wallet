package com.app.ewallet.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * JSON gửi topic {@code wallet.transfer.completed} — đủ để gửi mail cho cả người gửi và người nhận.
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
