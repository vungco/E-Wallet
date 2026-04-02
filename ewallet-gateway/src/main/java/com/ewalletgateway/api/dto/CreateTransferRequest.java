package com.ewalletgateway.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotBlank String requestId,
        @NotNull Long fromWalletId,
        @NotNull Long toWalletId,
        @NotNull Long toUserId,
        @NotNull @DecimalMin(value = "0.0001", inclusive = true) BigDecimal amount
) {
}
