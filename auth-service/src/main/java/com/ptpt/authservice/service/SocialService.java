package com.ptpt.authservice.service;

import com.ptpt.authservice.dto.SocialUserInfo;

public interface SocialService {
    SocialUserInfo getUserInfo(String accessToken);
}
