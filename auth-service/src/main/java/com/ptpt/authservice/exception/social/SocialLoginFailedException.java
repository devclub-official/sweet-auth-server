package com.ptpt.authservice.exception.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class SocialLoginFailedException extends AuthServiceException {
    public SocialLoginFailedException() {
        super(ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED);
    }

    public SocialLoginFailedException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED, customMessage);
    }
}
