package com.ptpt.authservice.exception.social;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;

public class SocialEmailAlreadyExistsException extends AuthServiceException {
    public SocialEmailAlreadyExistsException() {
        super(ApiResponseCode.AUTH_SOCIAL_EMAIL_ALREADY_EXISTS);
    }

    public SocialEmailAlreadyExistsException(String email) {
        super(ApiResponseCode.AUTH_SOCIAL_EMAIL_ALREADY_EXISTS,
                "해당 이메일로 이미 가입된 계정이 있습니다: " + email);
    }
}