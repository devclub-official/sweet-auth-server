package com.ptpt.authservice.enums;

public enum SocialProvider {
    KAKAO,
    NAVER,
    APPLE,
    NONE;

    public static SocialProvider fromString(String provider) {
        try {
            return SocialProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
        }
    }
}

