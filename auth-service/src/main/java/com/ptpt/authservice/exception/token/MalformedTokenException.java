package com.ptpt.authservice.exception.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class MalformedTokenException extends AuthServiceException {
    public MalformedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_MALFORMED);
    }

    public MalformedTokenException(String customMessage) {
        super(ApiResponseCode.AUTH_TOKEN_MALFORMED, customMessage);
    }
}
