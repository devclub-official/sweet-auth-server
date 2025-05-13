package com.ptpt.authservice.service;

import com.ptpt.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

//    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 밀리초 단위

    public void blacklistRefreshToken(String refreshToken) {
        // Refresh Token을 Redis에 추가 (만료 시간을 설정)
//        redisTemplate.opsForValue().set(refreshToken, "blacklisted", refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String refreshToken) {
        // Refresh Token이 블랙리스트에 있는지 확인
//        return redisTemplate.hasKey(refreshToken);
        return false;
    }
}
