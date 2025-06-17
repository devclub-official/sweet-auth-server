package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class ExpiredTokenException extends AuthServiceException {
    public ExpiredTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_EXPIRED);
    }

    public ExpiredTokenException(String customMessage) {
        super(ApiResponseCode.AUTH_TOKEN_EXPIRED, customMessage);
    }
}
