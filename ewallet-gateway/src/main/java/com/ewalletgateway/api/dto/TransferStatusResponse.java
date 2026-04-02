package com.ewalletgateway.api.dto;

public record TransferStatusResponse(
        String requestId,
        String status,
        long fromWalletId,
        long toWalletId,
        String amount,
        String errorMessage
) {
}
