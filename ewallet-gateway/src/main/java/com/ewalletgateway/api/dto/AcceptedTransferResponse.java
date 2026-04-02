package com.ewalletgateway.api.dto;

public record AcceptedTransferResponse(
        String requestId,
        String status
) {
}
