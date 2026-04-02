package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApiException {

    public InsufficientBalanceException() {
        super(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "Insufficient balance for debit");
    }
}
