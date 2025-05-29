package com.ptpt.authservice.exceptions.user;

import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;

public class UserNotFoundException extends AuthServiceException {
    public UserNotFoundException() {
        super(ApiResponseCode.USER_READ_FAILED, "사용자를 찾을 수 없습니다.");
    }

    public UserNotFoundException(String customMessage) {
        super(ApiResponseCode.USER_READ_FAILED, customMessage);
    }
}
