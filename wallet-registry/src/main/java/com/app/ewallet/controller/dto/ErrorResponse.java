package com.app.ewallet.controller.dto;

public record ErrorResponse(
        String code,
        String message
) {
}
