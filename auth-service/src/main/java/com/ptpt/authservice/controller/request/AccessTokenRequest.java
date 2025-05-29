package com.ptpt.authservice.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenRequest {
    private String provider; // "google", "kakao", "naver" 등
    private String accessToken;
}
