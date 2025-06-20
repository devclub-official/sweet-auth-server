package com.ptpt.authservice.enums;

import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum SocialSignupRequiredField {

    NICKNAME("nickname", "닉네임", true),
    BIRTH_DATE("birthDate", "생년월일", false),
    LOCATION("location", "거주지", true),
    INTERESTED_SPORTS("interestedSports", "관심 스포츠", true),
    PHONE_NUMBER("phoneNumber", "전화번호", false),
    PROFILE_IMAGE("profileImageUrl", "프로필 이미지", false),
    AGREE_TERMS("agreeTerms", "이용약관 동의", false),
    BIO("bio", "자기소개", false);

    private final String fieldName;
    private final String displayName;
    private final boolean required;

    /**
     * 필수 필드 목록 반환
     */
    public static List<String> getRequiredFields() {
        return Arrays.stream(values())
                .filter(SocialSignupRequiredField::isRequired)
                .map(SocialSignupRequiredField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 선택 필드 목록 반환
     */
    public static List<String> getOptionalFields() {
        return Arrays.stream(values())
                .filter(field -> !field.isRequired())
                .map(SocialSignupRequiredField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 모든 필드 목록 반환
     */
    public static List<String> getAllFields() {
        return Arrays.stream(values())
                .map(SocialSignupRequiredField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 필드별 설명 맵 반환
     */
    public static List<FieldInfo> getFieldInfoList() {
        return Arrays.stream(values())
                .map(field -> new FieldInfo(
                        field.getFieldName(),
                        field.getDisplayName(),
                        field.isRequired()
                ))
                .collect(Collectors.toList());
    }

    /**
     * CompleteSignupRequest 필수 필드 검증
     */
    public static void validateRequiredFields(CompleteSignupRequest request) {
        // 닉네임 검증
        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        // 생년월일 검증
//        if (request.getBirthDate() == null) {
//            throw new IllegalArgumentException("생년월일은 필수입니다.");
//        }

        // 거주지 검증
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("거주지는 필수입니다.");
        }

        // 관심 스포츠 검증
        if (request.getInterestedSports() == null || request.getInterestedSports().isEmpty()) {
            throw new IllegalArgumentException("관심 스포츠는 최소 1개 이상 선택해야 합니다.");
        }

        // 약관 동의 검증
//        if (request.getAgreeTerms() == null || !request.getAgreeTerms()) {
//            throw new IllegalArgumentException("서비스 이용약관에 동의해야 합니다.");
//        }
    }

    /**
     * 필드 정보 클래스
     */
    public static class FieldInfo {
        private final String fieldName;
        private final String displayName;
        private final boolean required;

        public FieldInfo(String fieldName, String displayName, boolean required) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.required = required;
        }

        // Getters
        public String getFieldName() { return fieldName; }
        public String getDisplayName() { return displayName; }
        public boolean isRequired() { return required; }
    }
}