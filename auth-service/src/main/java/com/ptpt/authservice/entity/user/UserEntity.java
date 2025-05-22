package com.ptpt.authservice.entity.user;

import com.ptpt.authservice.dto.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 150)
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // 소셜 로그인 관련 필드
    @Column(name = "social_id")
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type")
    private User.SocialType socialType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Domain 객체로 변환
    public User toDomain() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .bio(bio)
                .profileImage(profileImage)
                .phoneNumber(phoneNumber)
                .socialId(socialId)
                .socialType(socialType)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // Domain 객체에서 Entity 생성
    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .phoneNumber(user.getPhoneNumber())
                .socialId(user.getSocialId())
                .socialType(user.getSocialType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // 기존 Entity 업데이트
    public void updateFromDomain(User user) {
        if (user.getNickname() != null) {
            this.nickname = user.getNickname();
        }
        if (user.getBio() != null) {
            this.bio = user.getBio();
        }
        if (user.getProfileImage() != null) {
            this.profileImage = user.getProfileImage();
        }
        if (user.getPhoneNumber() != null) {
            this.phoneNumber = user.getPhoneNumber();
        }
        this.updatedAt = LocalDateTime.now();
    }
}
