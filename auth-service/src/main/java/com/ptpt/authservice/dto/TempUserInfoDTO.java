package com.ptpt.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TempUserInfoDTO {
    private String email;
    private String socialId;
    private User.SocialType socialType;
    private String nickname;
    private String profileImageUrl;

    // User Domain 객체로 변환 (추가 정보와 함께)
    public User toUserDomain(String phoneNumber) {
        return User.createSocialUser(
                this.email,
                this.nickname,
                this.socialId,
                this.socialType,
                this.profileImageUrl,
                phoneNumber
        );
    }
}
