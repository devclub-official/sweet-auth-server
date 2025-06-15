package com.ptpt.authservice.controller.response;

import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.enums.SocialSignupRequiredField;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SocialLoginResponse {

    private String status; // "LOGIN_SUCCESS" 또는 "SIGNUP_REQUIRED"

    // 로그인 성공 시
    private TokenResponse tokens;

    // 회원가입 필요 시
    private String tempToken;
    private TempUserInfo tempUserInfo;
    private List<String> requiredFields;
    private List<String> optionalFields;
    private List<SocialSignupRequiredField.FieldInfo> fieldInfoList;

    // 추가 옵션 데이터
    private List<String> sportsOptions; // 관심 스포츠 선택 옵션
    private List<String> locationOptions; // 거주지 선택 옵션

    // 필드별 상세 정보 (UI에서 활용)
    private SignupFormInfo signupFormInfo;

    @Data
    @Builder
    public static class SignupFormInfo {
        private NicknameInfo nickname;
        private BirthDateInfo birthDate;
        private LocationInfo location;
        private SportsInfo interestedSports;
        private PhoneNumberInfo phoneNumber;
        private ProfileImageInfo profileImage;
        private BioInfo bio;
    }

    @Data
    @Builder
    public static class NicknameInfo {
        private String label = "닉네임";
        private String placeholder = "사용할 닉네임을 입력하세요";
        private boolean required = true;
        private int minLength = 2;
        private int maxLength = 30;
        private String defaultValue; // 소셜에서 가져온 닉네임
    }

    @Data
    @Builder
    public static class BirthDateInfo {
        private String label = "생년월일";
        private String placeholder = "YYYY-MM-DD";
        private boolean required = true;
        private String minDate = "1900-01-01";
        private String maxDate; // 현재 날짜에서 14년 전
    }

    @Data
    @Builder
    public static class LocationInfo {
        private String label = "거주지";
        private boolean required = true;
        private List<String> options;
    }

    @Data
    @Builder
    public static class SportsInfo {
        private String label = "관심 스포츠";
        private String description = "관심있는 스포츠를 최대 5개까지 선택하세요";
        private boolean required = true;
        private int minSelection = 1;
        private int maxSelection = 5;
        private List<String> options;
    }

    @Data
    @Builder
    public static class PhoneNumberInfo {
        private String label = "전화번호";
        private String placeholder = "010-1234-5678";
        private boolean required = false;
        private String pattern = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$";
    }

    @Data
    @Builder
    public static class ProfileImageInfo {
        private String label = "프로필 이미지";
        private boolean required = false;
        private String defaultImageUrl; // 소셜에서 가져온 이미지
        private List<String> allowedFormats = List.of("jpg", "jpeg", "png", "webp");
        private int maxSizeMB = 5;
    }

    @Data
    @Builder
    public static class BioInfo {
        private String label = "자기소개";
        private String placeholder = "자신을 소개해주세요";
        private boolean required = false;
        private int maxLength = 500;
    }
}