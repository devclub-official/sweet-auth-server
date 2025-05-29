package com.ptpt.authservice.exceptions.token;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class MalformedTokenException extends AuthServiceException {
    public MalformedTokenException() {
        super(ApiResponseCode.AUTH_TOKEN_MALFORMED);
    }

    public MalformedTokenException(String customMessage) {
        super(ApiResponseCode.AUTH_TOKEN_MALFORMED, customMessage);
    }
}
