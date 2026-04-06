package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class WalletNotFoundException extends ApiException {

    public WalletNotFoundException(Long walletId) {
        super(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", "Wallet not found: " + walletId);
    }

    public WalletNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", message);
    }
}
