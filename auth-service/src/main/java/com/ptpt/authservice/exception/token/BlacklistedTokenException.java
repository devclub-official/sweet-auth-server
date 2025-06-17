package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class BlacklistedTokenException extends AuthServiceException {
    public BlacklistedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_BLACKLISTED);
    }
}
