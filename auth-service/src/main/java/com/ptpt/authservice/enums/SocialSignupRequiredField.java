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
    PHONE_NUMBER("phoneNumber", "전화번호", true),
    AGREE_TERMS("agreeTerms", "약관 동의 여부", true),
    NICKNAME("nickname", "닉네임", false), // 소셜에서 받아오지만 수정 가능
    BIO("bio", "자기소개", false);

    private final String fieldName;
    private final String displayName;
    private final boolean required;

    /**
     * 필수 필드만 반환
     */
    public static List<String> getRequiredFields() {
        return Arrays.stream(values())
                .filter(field -> field.required)
                .map(SocialSignupRequiredField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 모든 필드 반환
     */
    public static List<String> getAllFields() {
        return Arrays.stream(values())
                .map(SocialSignupRequiredField::getFieldName)
                .collect(Collectors.toList());
    }

    /**
     * 필드명으로 Enum 찾기
     */
    public static SocialSignupRequiredField fromFieldName(String fieldName) {
        return Arrays.stream(values())
                .filter(field -> field.fieldName.equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필드명입니다: " + fieldName));
    }

    /**
     * 필수 필드 검증
     */
    public static void validateRequiredFields(CompleteSignupRequest request) {
        for (SocialSignupRequiredField field : values()) {
            if (field.required && !field.isFieldProvided(request)) {
                throw new IllegalArgumentException(field.displayName + "는 필수 입력 항목입니다.");
            }
        }
    }

    /**
     * 각 필드별 값 제공 여부 확인
     */
    private boolean isFieldProvided(CompleteSignupRequest request) {
        return switch (this) {
            case PHONE_NUMBER -> request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty();
            case AGREE_TERMS -> request.isAgreeTerms();
            case NICKNAME -> request.getNickname() != null && !request.getNickname().trim().isEmpty();
            case BIO -> true; // bio는 선택사항이므로 항상 true
        };
    }
}
