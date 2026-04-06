package com.ewalletgateway.api.dto;

public record RegisterResponse(
        Long userId,
        String email,
        String name,
        String message
) {
}
