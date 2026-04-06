package com.ewalletgateway.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WalletFundRequest(
        @NotNull @DecimalMin(value = "0.0001", inclusive = true) BigDecimal amount,
        @NotBlank @Size(max = 128) String idempotencyKey
) {
}
