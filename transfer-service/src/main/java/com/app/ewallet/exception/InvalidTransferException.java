package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransferException extends ApiException {

    public InvalidTransferException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVALID_TRANSFER", message);
    }
}
