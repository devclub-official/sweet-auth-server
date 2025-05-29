package com.ptpt.authservice.exceptions.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class BlacklistedTokenException extends AuthServiceException {
    public BlacklistedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_BLACKLISTED);
    }
}
