package com.ptpt.authservice.dto;

import com.ptpt.authservice.controller.response.TokenResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SocialLoginResponse {
    private String status; // "LOGIN_SUCCESS" 또는 "SIGNUP_REQUIRED"
    private String tempToken; // 임시 토큰 (회원가입 필요한 경우)
    private TokenResponse tokens; // 로그인 성공한 경우
    private TempUserInfo tempUserInfo; // 임시 사용자 정보
    private List<String> requiredFields; // 필수 입력 필드들
}
