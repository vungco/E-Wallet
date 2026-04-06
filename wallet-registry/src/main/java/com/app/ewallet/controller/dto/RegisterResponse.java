package com.app.ewallet.controller.dto;

public record RegisterResponse(
        Long userId,
        String email,
        String name,
        String message
) {
}
