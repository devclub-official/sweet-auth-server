package com.ptpt.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ptpt.authservice.entity.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// SpringSecurity 관련 데이터를 넘어준다.

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

    // 소셜 로그인 관련 필드
    private String socialId;
    private SocialType socialType;

    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 소셜 타입 열거형
    public enum SocialType {
        KAKAO, GOOGLE, NAVER, NONE
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
//        return String.valueOf(this.id);
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
        return socialType != null && socialType != SocialType.NONE;
    }

    // 일반 로그인 사용자인지 확인
    public boolean isNormalUser() {
        return socialType == null || socialType == SocialType.NONE;
    }

    // 사용자 정보 업데이트를 위한 메서드
    public User updateProfile(String nickname, String bio, String profileImage, String phoneNumber) {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .nickname(nickname != null ? nickname : this.nickname)
                .bio(bio != null ? bio : this.bio)
                .profileImage(profileImage != null ? profileImage : this.profileImage)
                .phoneNumber(phoneNumber != null ? phoneNumber : this.phoneNumber)
                .socialId(this.socialId)
                .socialType(this.socialType)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 소셜 사용자 생성을 위한 정적 메서드
    public static User createSocialUser(String email, String nickname, String socialId,
                                        SocialType socialType, String profileImage, String phoneNumber) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password("") // 소셜 로그인 사용자는 비밀번호 없음
                .socialId(socialId)
                .socialType(socialType)
                .profileImage(profileImage)
                .phoneNumber(phoneNumber)
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
                .socialType(SocialType.NONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateUserInfo(String nickname, String phoneNumber, String profileImage) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
