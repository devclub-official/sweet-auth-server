package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.exceptions.AuthException;
import com.ptpt.authservice.exceptions.token.InvalidTokenException;
import com.ptpt.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * 사용자에 대한 액세스 토큰과 리프레시 토큰 생성
     * SecurityContext 설정은 JWT 필터에서 담당
     */
    public TokenResponse generateTokens(User user) {
        log.info("토큰 생성 시작 - userId: {}, email: {}", user.getId(), user.getEmail());

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("토큰 생성 완료 - userId: {}", user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiration)
                .refreshTokenExpiresIn(refreshTokenExpiration)
                .build();
    }

    /**
     * UserDetails 기반 토큰 생성
     */
//    public TokenResponse generateTokens(UserDetails userDetails) {
//        String accessToken = jwtUtil.generateAccessToken(userDetails);
//        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
//
//        return TokenResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .accessTokenExpiresIn(accessTokenExpiration)
//                .refreshTokenExpiresIn(refreshTokenExpiration)
//                .build();
//    }

    /**
     * 임시 토큰 생성
     */
    public String generateTempToken(TempUserInfo tempUserInfo) {
        log.info("임시 토큰 생성 - email: {}", tempUserInfo.getEmail());
        return jwtUtil.generateTempToken(tempUserInfo);
    }

    /**
     * 임시 토큰 검증 및 정보 추출
     */
    public TempUserInfo validateAndExtractTempToken(String tempToken) {
        if (!jwtUtil.validateToken(tempToken) || !jwtUtil.isTempToken(tempToken)) {
            throw new InvalidTokenException("유효하지 않은 임시 토큰입니다.");
        }

        return jwtUtil.extractTempUserInfo(tempToken);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String extractEmail(String token) {
        return jwtUtil.extractEmail(token);
    }

    /**
     * 리프레시 토큰인지 확인
     */
    public boolean isRefreshToken(String token) {
        return jwtUtil.isRefreshToken(token);
    }
}
