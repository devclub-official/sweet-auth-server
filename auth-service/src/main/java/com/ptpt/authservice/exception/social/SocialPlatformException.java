package com.ptpt.authservice.exception.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class SocialPlatformException extends AuthServiceException {
    public SocialPlatformException() {
        super(ApiResponseCode.AUTH_SOCIAL_PLATFORM_ERROR);
    }

    public SocialPlatformException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_PLATFORM_ERROR, customMessage);
    }
}
