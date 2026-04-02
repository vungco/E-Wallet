package com.app.ewallet.api.dto;

public record AcceptedTransferResponse(
        String requestId,
        String status
) {
}
