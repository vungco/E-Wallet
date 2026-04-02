package com.app.ewallet.controller;

import com.app.ewallet.config.OpenApiConfig;
import com.app.ewallet.controller.dto.AmountRequest;
import com.app.ewallet.controller.dto.InternalWalletOperationResponse;
import com.app.ewallet.exception.ApiException;
import com.app.ewallet.service.interfaces.IWalletLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1")
@Tag(name = "Internal")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_API_KEY_SCHEME)
@RequiredArgsConstructor
public class InternalWalletController {

    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";

    private final IWalletLedgerService walletLedgerService;

    @PostMapping("/wallets/{walletId}/debit")
    @Operation(summary = "Trừ tiền ví (nội bộ, có idempotency)")
    public InternalWalletOperationResponse debit(
            @PathVariable Long walletId,
            @Parameter(description = "Bắt buộc theo saga; cùng key + cùng tham số → trả kết quả đã xử lý")
            @RequestHeader(HEADER_IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody AmountRequest body
    ) {
        String key = requireIdempotencyKey(idempotencyKey);
        return walletLedgerService.debit(walletId, key, body.amount());
    }

    @PostMapping("/wallets/{walletId}/credit")
    @Operation(summary = "Cộng tiền ví (nội bộ, có idempotency)")
    public InternalWalletOperationResponse credit(
            @PathVariable Long walletId,
            @Parameter(description = "Bắt buộc theo saga; cùng key + cùng tham số → trả kết quả đã xử lý")
            @RequestHeader(HEADER_IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody AmountRequest body
    ) {
        String key = requireIdempotencyKey(idempotencyKey);
        return walletLedgerService.credit(walletId, key, body.amount());
    }

    private static String requireIdempotencyKey(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_IDEMPOTENCY_KEY",
                    "Idempotency-Key header must not be blank");
        }
        return idempotencyKey.trim();
    }
}
