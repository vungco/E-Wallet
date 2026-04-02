package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRequestException extends ApiException {

    public DuplicateRequestException(String requestId) {
        super(HttpStatus.CONFLICT, "DUPLICATE_REQUEST", "Transfer requestId already exists: " + requestId);
    }
}
