package com.ptpt.authservice.exceptions.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class SocialTokenInvalidException extends AuthServiceException {
    public SocialTokenInvalidException() {
        super(ApiResponseCode.AUTH_SOCIAL_TOKEN_INVALID);
    }

    public SocialTokenInvalidException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_TOKEN_INVALID, customMessage);
    }
}
