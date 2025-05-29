package com.ptpt.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    private String socialId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String provider;
}
