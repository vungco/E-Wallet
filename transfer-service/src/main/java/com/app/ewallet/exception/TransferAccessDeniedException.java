package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class TransferAccessDeniedException extends ApiException {

    public TransferAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "TRANSFER_ACCESS_DENIED", "Not a participant of this transfer");
    }
}
