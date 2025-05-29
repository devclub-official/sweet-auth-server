package com.ptpt.authservice.exceptions.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class SocialLoginFailedException extends AuthServiceException {
    public SocialLoginFailedException() {
        super(ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED);
    }

    public SocialLoginFailedException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED, customMessage);
    }
}
