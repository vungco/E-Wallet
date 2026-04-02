package com.app.ewallet.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull @DecimalMin(value = "0.0001", inclusive = true, message = "amount must be positive")
        BigDecimal amount
) {
}
