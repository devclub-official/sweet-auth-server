package com.ptpt.authservice.exceptions.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class UnsupportedTokenException extends AuthServiceException {
    public UnsupportedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_UNSUPPORTED);
    }
}
