package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class WalletAccessDeniedException extends ApiException {

    public WalletAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "WALLET_ACCESS_DENIED", "You do not have access to this wallet");
    }
}
