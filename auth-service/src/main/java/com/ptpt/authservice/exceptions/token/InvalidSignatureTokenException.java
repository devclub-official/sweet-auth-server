package com.ptpt.authservice.exceptions.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class InvalidSignatureTokenException extends AuthServiceException {
    public InvalidSignatureTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_INVALID_SIGNATURE);
    }
}
