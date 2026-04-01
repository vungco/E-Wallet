package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email already registered: " + email);
    }
}
