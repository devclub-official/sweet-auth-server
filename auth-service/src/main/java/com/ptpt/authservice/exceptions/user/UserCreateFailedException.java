package com.ptpt.authservice.exceptions.user;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class UserCreateFailedException extends AuthServiceException {
    public UserCreateFailedException() {
        super(ApiResponseCode.USER_CREATE_FAILED);
    }

    public UserCreateFailedException(String customMessage) {
        super(ApiResponseCode.USER_CREATE_FAILED, customMessage);
    }
}
