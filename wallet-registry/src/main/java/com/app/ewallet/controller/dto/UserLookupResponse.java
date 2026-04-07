package com.app.ewallet.controller.dto;

/** Kết quả tra user theo email (chuyển tiền — không gồm số dư ví). */
public record UserLookupResponse(long userId, long walletId, String email, String name) {
}
