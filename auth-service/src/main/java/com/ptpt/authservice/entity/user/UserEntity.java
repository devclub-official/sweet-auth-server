package com.ptpt.authservice.entity.user;

import com.ptpt.authservice.dto.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_nickname", columnList = "nickname"),
        @Index(name = "idx_user_social", columnList = "social_id, social_type"),
        @Index(name = "idx_user_created_at", columnList = "created_at"),
        @Index(name = "idx_user_last_login", columnList = "last_login_at")
})
@Where(clause = "deleted_at IS NULL") // 소프트 삭제 자동 필터링
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = {"password"})
@EqualsAndHashCode(of = "id")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password; // 소셜 로그인 사용자는 null

    @Column(name = "nickname", nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(name = "bio", length = 150)
    private String bio;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // UserEntity.java에 추가해야 할 필드들
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "interested_sports", columnDefinition = "TEXT")
    private String interestedSports; // JSON 문자열

    @Column(name = "social_profile_image", length = 500)
    private String socialProfileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 20, nullable = false)
    @Builder.Default
    private User.UserType userType = User.UserType.NORMAL;

    @Column(name = "agree_terms", nullable = false)
    @Builder.Default
    private Boolean agreeTerms = false;

    @Column(name = "agree_privacy", nullable = false)
    @Builder.Default
    private Boolean agreePrivacy = false;

    @Column(name = "agree_marketing", nullable = false)
    @Builder.Default
    private Boolean agreeMarketing = false;

    // ===== 소셜 로그인 관련 필드 =====

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", length = 20)
    private User.SocialType socialType;

    // ===== 계정 상태 관련 필드 =====

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ===== 타임스탬프 필드 =====

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 소프트 삭제용

    // ===== JPA 라이프사이클 콜백 =====

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // 일반 가입 사용자는 비밀번호 변경 시간 설정
        if (this.password != null) {
            this.passwordChangedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 소셜 로그인 사용자 여부 확인
     */
    public boolean isSocialUser() {
        return this.socialId != null && this.socialType != null;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        if (isSocialUser()) {
            throw new IllegalStateException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.enabled = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 이메일 인증 완료
     */
    public void verifyEmail() {
        this.emailVerified = true;
    }

    // ===== Domain 변환 메서드 =====

    /**
     * Entity를 Domain 객체로 변환
     */
    public User toDomain() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .bio(bio)
                .profileImage(profileImage)
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)          // 추가
                .location(location)            // 추가
                .interestedSports(interestedSports)  // 추가
                .socialId(socialId)
                .socialType(socialType)
                .socialProfileImage(socialProfileImage)  // 추가
                .userType(userType)            // 추가
                .enabled(enabled)
                .emailVerified(emailVerified)
                .agreeTerms(agreeTerms)        // 추가
                .agreePrivacy(agreePrivacy)    // 추가
                .agreeMarketing(agreeMarketing) // 추가
                .lastLoginAt(lastLoginAt)
                .passwordChangedAt(passwordChangedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();
    }

    /**
     * Domain 객체로부터 Entity 생성
     */
    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())        // 추가
                .location(user.getLocation())          // 추가
                .interestedSports(user.getInterestedSports())  // 추가
                .socialId(user.getSocialId())
                .socialType(user.getSocialType())
                .socialProfileImage(user.getSocialProfileImage())  // 추가
                .userType(user.getUserType())          // 추가
                .enabled(user.getEnabled() != null ? user.getEnabled() : true)
                .emailVerified(user.getEmailVerified() != null ? user.getEmailVerified() : false)
                .agreeTerms(user.getAgreeTerms() != null ? user.getAgreeTerms() : false)        // 추가
                .agreePrivacy(user.getAgreePrivacy() != null ? user.getAgreePrivacy() : false)  // 추가
                .agreeMarketing(user.getAgreeMarketing() != null ? user.getAgreeMarketing() : false)  // 추가
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    /**
     * Domain 객체로부터 기존 Entity 업데이트
     */
    public void updateFromDomain(User user) {
        // 닉네임 업데이트
        if (user.getNickname() != null && !user.getNickname().equals(this.nickname)) {
            this.nickname = user.getNickname();
        }

        // 자기소개 업데이트
        if (user.getBio() != null) {
            this.bio = user.getBio();
        }

        // 프로필 이미지 업데이트
        if (user.getProfileImage() != null) {
            this.profileImage = user.getProfileImage();
        }

        // 전화번호 업데이트
        if (user.getPhoneNumber() != null) {
            this.phoneNumber = user.getPhoneNumber();
        }

        // 계정 상태 업데이트
        if (user.getEnabled() != null) {
            this.enabled = user.getEnabled();
        }

        // 이메일 인증 상태 업데이트
        if (user.getEmailVerified() != null) {
            this.emailVerified = user.getEmailVerified();
        }

        // 새로 추가된 필드들
        if (user.getBirthDate() != null) {
            this.birthDate = user.getBirthDate();
        }

        if (user.getLocation() != null) {
            this.location = user.getLocation();
        }

        if (user.getInterestedSports() != null) {
            this.interestedSports = user.getInterestedSports();
        }

        if (user.getSocialProfileImage() != null) {
            this.socialProfileImage = user.getSocialProfileImage();
        }

        if (user.getUserType() != null) {
            this.userType = user.getUserType();
        }

        // 약관 동의 관련
        if (user.getAgreeTerms() != null) {
            this.agreeTerms = user.getAgreeTerms();
        }

        if (user.getAgreePrivacy() != null) {
            this.agreePrivacy = user.getAgreePrivacy();
        }

        if (user.getAgreeMarketing() != null) {
            this.agreeMarketing = user.getAgreeMarketing();
        }
    }

    /**
     * 사용자 정보 업데이트 (선택적 필드만)
     */
    public void updateUserInfo(String nickname, String bio, String phoneNumber, String profileImage) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }
}