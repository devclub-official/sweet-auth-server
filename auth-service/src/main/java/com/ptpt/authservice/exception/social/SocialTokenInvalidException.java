package com.ptpt.authservice.exception.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class SocialTokenInvalidException extends AuthServiceException {
    public SocialTokenInvalidException() {
        super(ApiResponseCode.AUTH_SOCIAL_TOKEN_INVALID);
    }

    public SocialTokenInvalidException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_TOKEN_INVALID, customMessage);
    }
}
