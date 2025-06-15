package com.ptpt.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.entity.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.List;

// SpringSecurity 관련 데이터를 넘어준다.

@Slf4j
@Getter
@Builder
public class User implements UserDetails {

    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private String nickname;
    private String profileImage;
    private String phoneNumber;
    private String bio;

    // 추가된 프로필 정보
    private LocalDate birthDate; // 생년월일
    private String location; // 거주지
    private String interestedSports; // 관심 스포츠 (JSON 문자열)

    // 소셜 로그인 관련 필드
    private String socialId;
    private SocialType socialType;
    private String socialProfileImage; // 소셜에서 가져온 원본 프로필 이미지

    // 사용자 타입
    private UserType userType;

    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean agreeTerms;
    private Boolean agreePrivacy;
    private Boolean agreeMarketing;

    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 소셜 타입 열거형
    public enum SocialType {
        KAKAO, GOOGLE, NAVER, APPLE, NONE
    }

    // 사용자 타입 열거형
    public enum UserType {
        NORMAL, SOCIAL
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .nickname(nickname)
                .password(password)
                .email(email)
                .build();
    }

    // 소셜 로그인 사용자인지 확인
    public boolean isSocialUser() {
        return userType == UserType.SOCIAL;
    }

    // 일반 로그인 사용자인지 확인
    public boolean isNormalUser() {
        return userType == UserType.NORMAL;
    }

    /**
     * 관심 스포츠 JSON 문자열을 List로 변환
     */
    public List<String> getInterestedSportsList() {
        if (interestedSports == null || interestedSports.isEmpty()) {
            return List.of();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(interestedSports, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("관심 스포츠 JSON 파싱 실패 - userId: {}, json: {}", id, interestedSports);
            return List.of();
        }
    }

    /**
     * 나이 계산
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 프로필 이미지 URL 반환 (우선순위: 직접 업로드 > 소셜 이미지)
     */
    public String getDisplayProfileImage() {
        if (profileImage != null && !profileImage.isEmpty()) {
            return profileImage;
        }
        return socialProfileImage;
    }

    // 사용자 정보 업데이트를 위한 메서드
    public User updateProfile(String nickname, String bio, String profileImage, String phoneNumber,
                              LocalDate birthDate, String location, List<String> interestedSportsList) {

        String interestedSportsJson = null;
        if (interestedSportsList != null && !interestedSportsList.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                interestedSportsJson = objectMapper.writeValueAsString(interestedSportsList);
            } catch (JsonProcessingException e) {
                log.warn("관심 스포츠 JSON 변환 실패 - userId: {}", id);
                interestedSportsJson = this.interestedSports; // 기존 값 유지
            }
        }

        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(nickname != null ? nickname : this.nickname)
                .bio(bio != null ? bio : this.bio)
                .profileImage(profileImage != null ? profileImage : this.profileImage)
                .phoneNumber(phoneNumber != null ? phoneNumber : this.phoneNumber)
                .birthDate(birthDate != null ? birthDate : this.birthDate)
                .location(location != null ? location : this.location)
                .interestedSports(interestedSportsJson != null ? interestedSportsJson : this.interestedSports)
                .socialId(this.socialId)
                .socialType(this.socialType)
                .socialProfileImage(this.socialProfileImage)
                .userType(this.userType)
                .enabled(this.enabled)
                .emailVerified(this.emailVerified)
                .agreeTerms(this.agreeTerms)
                .agreePrivacy(this.agreePrivacy)
                .agreeMarketing(this.agreeMarketing)
                .lastLoginAt(this.lastLoginAt)
                .passwordChangedAt(this.passwordChangedAt)
                .deletedAt(this.deletedAt)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 소셜 사용자 생성을 위한 정적 메서드
    public static User createSocialUser(String email, String nickname, String socialId, SocialType socialType,
                                        String socialProfileImage, String phoneNumber, LocalDate birthDate,
                                        String location, String interestedSports, String profileImage, String bio) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password(null) // 소셜 로그인 사용자는 비밀번호 없음
                .bio(bio)
                .profileImage(profileImage) // 사용자가 직접 업로드한 이미지
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)
                .location(location)
                .interestedSports(interestedSports) // JSON 문자열
                .socialId(socialId)
                .socialType(socialType)
                .socialProfileImage(socialProfileImage) // 소셜에서 가져온 이미지
                .userType(UserType.SOCIAL)
                .enabled(true)
                .emailVerified(true) // 소셜 로그인은 이메일 인증 완료로 간주
                .agreeTerms(true)
                .agreePrivacy(true)
                .agreeMarketing(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 일반 사용자 생성을 위한 정적 메서드
    public static User createNormalUser(String email, String nickname, String password) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .userType(UserType.NORMAL)
                .socialType(SocialType.NONE)
                .enabled(true)
                .emailVerified(false) // 일반 가입은 이메일 인증 필요
                .agreeTerms(true)
                .agreePrivacy(true)
                .agreeMarketing(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 기본 사용자 정보 업데이트 (기존 호환성 유지)
    public void updateUserInfo(String nickname, String phoneNumber, String profileImage) {
        updateUserInfo(nickname, phoneNumber, profileImage, null, null, null);
    }

    // 확장된 사용자 정보 업데이트
    public void updateUserInfo(String nickname, String phoneNumber, String profileImage,
                               LocalDate birthDate, String location, List<String> interestedSportsList) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (location != null) {
            this.location = location;
        }
        if (interestedSportsList != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                this.interestedSports = objectMapper.writeValueAsString(interestedSportsList);
            } catch (JsonProcessingException e) {
                log.warn("관심 스포츠 JSON 변환 실패 - userId: {}", id);
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 프로필 요약 정보 반환 (API 응답용)
     */
    public UserProfileSummary getProfileSummary() {
        return UserProfileSummary.builder()
                .id(this.id)
                .email(this.email)
                .nickname(this.nickname)
                .profileImage(getDisplayProfileImage())
                .bio(this.bio)
                .location(this.location)
                .age(getAge())
                .interestedSports(getInterestedSportsList())
                .userType(this.userType.name())
                .isEmailVerified(this.emailVerified)
                .createdAt(this.createdAt)
                .build();
    }

    @Builder
    @Getter
    public static class UserProfileSummary {
        private Long id;
        private String email;
        private String nickname;
        private String profileImage;
        private String bio;
        private String location;
        private Integer age;
        private List<String> interestedSports;
        private String userType;
        private Boolean isEmailVerified;
        private LocalDateTime createdAt;
    }
}