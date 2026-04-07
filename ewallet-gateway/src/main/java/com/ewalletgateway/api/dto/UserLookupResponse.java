package com.ewalletgateway.api.dto;

public record UserLookupResponse(long userId, long walletId, String email, String name) {
}
