package com.ptpt.authservice.exceptions.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class SocialPlatformException extends AuthServiceException {
    public SocialPlatformException() {
        super(ApiResponseCode.AUTH_SOCIAL_PLATFORM_ERROR);
    }

    public SocialPlatformException(String customMessage) {
        super(ApiResponseCode.AUTH_SOCIAL_PLATFORM_ERROR, customMessage);
    }
}
