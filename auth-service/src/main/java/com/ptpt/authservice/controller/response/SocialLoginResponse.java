package com.ptpt.authservice.controller.response;

import com.ptpt.authservice.dto.TempUserInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SocialLoginResponse {
    private String status; // "LOGIN_SUCCESS" 또는 "SIGNUP_REQUIRED"
    private String tempToken; // 임시 토큰 (회원가입 필요한 경우)
    private TokenResponse tokens; // 로그인 성공한 경우
    private TempUserInfo tempUserInfo; // 임시 사용자 정보
    private List<String> requiredFields; // 필수 입력 필드들
    private Map<String, String> fieldDescriptions; // 필드 설명 추가
}
