package com.ptpt.authservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TempUserInfo {
    private String email;
    private String socialId;
    private User.SocialType socialType;
    private String nickname;
    private String profileImageUrl; // 소셜에서 가져온 프로필 이미지

    /**
     * User Domain 객체로 변환 (기존 호환성 유지)
     * @deprecated 새로운 toUserDomain(CompleteSignupRequest) 메서드 사용 권장
     */
    @Deprecated
    public User toUserDomain(String phoneNumber) {
        return User.createSocialUser(
                this.email,
                this.nickname,
                this.socialId,
                this.socialType,
                this.profileImageUrl, // socialProfileImage
                phoneNumber,
                null, // birthDate
                null, // location
                null, // interestedSports
                null, // profileImage (사용자 업로드)
                null  // bio
        );
    }

    /**
     * 완전한 회원가입 정보로 User Domain 객체 생성
     */
    public User toUserDomain(String phoneNumber, LocalDate birthDate, String location,
                             String interestedSportsJson, String userUploadedImage, String bio) {
        return User.createSocialUser(
                this.email,
                this.nickname,
                this.socialId,
                this.socialType,
                this.profileImageUrl, // 소셜에서 가져온 이미지
                phoneNumber,
                birthDate,
                location,
                interestedSportsJson,
                userUploadedImage, // 사용자가 직접 업로드한 이미지
                bio
        );
    }

    /**
     * CompleteSignupRequest로부터 User Domain 객체 생성
     */
    public User toUserDomain(com.ptpt.authservice.controller.request.CompleteSignupRequest request,
                             String interestedSportsJson) {
        return User.createSocialUser(
                this.email,
                request.getNickname() != null ? request.getNickname() : this.nickname,
                this.socialId,
                this.socialType,
                this.profileImageUrl, // 소셜에서 가져온 이미지
                request.getPhoneNumber(),
                request.getBirthDate(),
                request.getLocation(),
                interestedSportsJson,
                request.getProfileImageUrl(), // 사용자가 새로 업로드한 이미지
                request.getBio()
        );
    }

    /**
     * 임시 사용자 정보 검증
     */
    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (socialId == null || socialId.trim().isEmpty()) {
            throw new IllegalArgumentException("소셜 ID는 필수입니다.");
        }
        if (socialType == null) {
            throw new IllegalArgumentException("소셜 타입은 필수입니다.");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
    }

    /**
     * 소셜 플랫폼 표시명 반환
     */
    public String getSocialPlatformDisplayName() {
        if (socialType == null) {
            return "알 수 없음";
        }

        return switch (socialType) {
            case KAKAO -> "카카오";
            case NAVER -> "네이버";
            case GOOGLE -> "구글";
            case APPLE -> "애플";
            default -> "기타";
        };
    }

    /**
     * 프로필 이미지가 있는지 확인
     */
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }

    /**
     * 임시 사용자 정보 요약 (로그용)
     */
    public String getSummary() {
        return String.format("TempUser{email='%s', socialType=%s, nickname='%s', hasImage=%b}",
                email, socialType, nickname, hasProfileImage());
    }
}