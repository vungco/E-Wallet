package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends ApiException {

    public IdempotencyConflictException() {
        super(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT",
                "Idempotency-Key already used with different wallet, operation, or amount");
    }
}
