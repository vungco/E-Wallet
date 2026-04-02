package com.app.ewallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterResponse(
        @Schema(description = "Id user vừa tạo")
        Long userId,
        String email,
        String name,
        @Schema(description = "Gợi ý: gọi POST /api/v1/auth/login để nhận access + refresh token")
        String message
) {
}
