package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class UnsupportedTokenException extends AuthServiceException {
    public UnsupportedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_UNSUPPORTED);
    }
}
