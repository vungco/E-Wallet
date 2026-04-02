package com.app.ewallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record InternalWalletOperationResponse(
        @Schema(description = "Id ví")
        Long walletId,
        @Schema(description = "Số dư sau thao tác")
        BigDecimal balanceAfter,
        @Schema(description = "Phiên bản optimistic lock sau cập nhật")
        Long version,
        @Schema(description = "true nếu trả về từ bản ghi idempotency (đã xử lý trước đó)")
        boolean replayed
) {
}
