package com.ewalletgateway.api.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
