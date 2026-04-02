package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class TransferNotFoundException extends ApiException {

    public TransferNotFoundException(String requestId) {
        super(HttpStatus.NOT_FOUND, "TRANSFER_NOT_FOUND", "Transfer not found: " + requestId);
    }
}
