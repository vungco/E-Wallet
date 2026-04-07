package com.app.ewallet.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundByEmailException extends ApiException {

    public UserNotFoundByEmailException(String email) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "No user found for email: " + email);
    }
}
