package com.ptpt.authservice.exceptions.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class InvalidTokenException extends AuthServiceException {
    public InvalidTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_INVALID);
    }

    public InvalidTokenException(String customMessage) {
        super(ApiResponseCode.AUTH_TOKEN_INVALID, customMessage);
    }
}