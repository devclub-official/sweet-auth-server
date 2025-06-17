package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class InvalidTokenException extends AuthServiceException {
    public InvalidTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_INVALID);
    }

    public InvalidTokenException(String customMessage) {
        super(ApiResponseCode.AUTH_TOKEN_INVALID, customMessage);
    }
}