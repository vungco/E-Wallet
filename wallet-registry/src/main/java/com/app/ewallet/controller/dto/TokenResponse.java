package com.app.ewallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "JWT access token (Bearer)")
        String accessToken,
        @Schema(description = "Refresh token — chỉ lưu hash SHA-256 phía server")
        String refreshToken,
        @Schema(description = "Luôn là Bearer")
        String tokenType,
        @Schema(description = "Thời gian sống access token (giây)")
        long expiresInSeconds
) {
}
