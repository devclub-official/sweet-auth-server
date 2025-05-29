package com.ptpt.authservice.util;

import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.exceptions.token.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private static final String TOKEN_TYPE_KEY = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String TOKEN_TYPE_TEMP = "TEMP";

    private final SecretKey secretKey;
    private final JwtParser jwtParser;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.temp-token-expiration:1800000}")
    private long tempTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
                .setSigningKey(secretKey)
                .build();
    }

    // ===== Access Token Methods =====

    /**
     * Authentication 객체로부터 Access Token 생성
     */
//    public String generateAccessToken(Authentication authentication) {
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        return generateAccessToken(userDetails);
//    }

    /**
     * UserDetails 객체로부터 Access Token 생성
     */
//    public String generateAccessToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put(TOKEN_TYPE_KEY, TOKEN_TYPE_ACCESS);
//        return createToken(userDetails.getUsername(), claims, accessTokenExpiration);
//    }

    /**
     * User 객체로부터 Access Token 생성
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_KEY, TOKEN_TYPE_ACCESS);
        claims.put("userId", user.getId());
        claims.put("nickname", user.getNickname());
        return createToken(user.getEmail(), claims, accessTokenExpiration);
    }

    // ===== Refresh Token Methods =====

    /**
     * Authentication 객체로부터 Refresh Token 생성
     */
//    public String generateRefreshToken(Authentication authentication) {
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        return generateRefreshToken(userDetails);
//    }

    /**
     * UserDetails 객체로부터 Refresh Token 생성
     */
//    public String generateRefreshToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put(TOKEN_TYPE_KEY, TOKEN_TYPE_REFRESH);
//        return createToken(userDetails.getUsername(), claims, refreshTokenExpiration);
//    }

    /**
     * User 객체로부터 Refresh Token 생성
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_KEY, TOKEN_TYPE_REFRESH);
        return createToken(user.getEmail(), claims, refreshTokenExpiration);
    }

    // ===== Temp Token Methods =====

    /**
     * 임시 사용자 정보로부터 Temp Token 생성
     */
    public String generateTempToken(TempUserInfo tempUserInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_KEY, TOKEN_TYPE_TEMP);
        claims.put("email", tempUserInfo.getEmail());
        claims.put("socialId", tempUserInfo.getSocialId());
        claims.put("socialType", tempUserInfo.getSocialType().name());
        claims.put("nickname", tempUserInfo.getNickname());
        claims.put("profileImageUrl", tempUserInfo.getProfileImageUrl());

        log.debug("임시 토큰 생성 - email: {}", tempUserInfo.getEmail());
        return createToken("TEMP_USER", claims, tempTokenExpiration);
    }

    /**
     * Temp Token으로부터 임시 사용자 정보 추출
     */
    public TempUserInfo extractTempUserInfo(String tempToken) {
        validateTempToken(tempToken);

        Claims claims = extractAllClaims(tempToken);
        return TempUserInfo.builder()
                .email(claims.get("email", String.class))
                .socialId(claims.get("socialId", String.class))
                .socialType(User.SocialType.valueOf(claims.get("socialType", String.class)))
                .nickname(claims.get("nickname", String.class))
                .profileImageUrl(claims.get("profileImageUrl", String.class))
                .build();
    }

    // ===== Token Validation Methods =====

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰");
            throw new ExpiredTokenException();
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰");
            throw new UnsupportedTokenException();
        } catch (MalformedJwtException e) {
            log.warn("잘못된 형식의 JWT 토큰");
            throw new MalformedTokenException();
        } catch (SignatureException e) {
            log.warn("잘못된 서명의 JWT 토큰");
            throw new InvalidSignatureTokenException();
        } catch (JwtException e) {
            log.warn("잘못된 JWT 토큰");
            throw new InvalidTokenException();
        }
    }

    /**
     * Access Token인지 확인
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return TOKEN_TYPE_ACCESS.equals(claims.get(TOKEN_TYPE_KEY));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh Token인지 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return TOKEN_TYPE_REFRESH.equals(claims.get(TOKEN_TYPE_KEY));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Temp Token인지 확인
     */
    public boolean isTempToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return TOKEN_TYPE_TEMP.equals(claims.get(TOKEN_TYPE_KEY));
        } catch (Exception e) {
            return false;
        }
    }

    // ===== Token Information Extraction Methods =====

    /**
     * 토큰에서 이메일(subject) 추출
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 토큰에서 만료 시간 추출
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 특정 claim 추출
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ===== Private Helper Methods =====

    /**
     * JWT 토큰 생성 공통 로직
     */
    private String createToken(String subject, Map<String, Object> claims, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 모든 Claims 추출
     */
    private Claims extractAllClaims(String token) {
        try {
            return jwtParser
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedTokenException();
        } catch (MalformedJwtException e) {
            throw new MalformedTokenException();
        } catch (SignatureException e) {
            throw new InvalidSignatureTokenException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    /**
     * Temp Token 검증
     */
    private void validateTempToken(String token) {
        if (!isTempToken(token)) {
            throw new InvalidTokenException("임시 토큰이 아닙니다.");
        }
        if (isTokenExpired(token)) {
            throw new ExpiredTokenException("만료된 임시 토큰입니다.");
        }
    }
}