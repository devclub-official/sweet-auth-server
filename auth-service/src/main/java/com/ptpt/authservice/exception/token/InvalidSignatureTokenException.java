package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class InvalidSignatureTokenException extends AuthServiceException {
    public InvalidSignatureTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_INVALID_SIGNATURE);
    }
}
