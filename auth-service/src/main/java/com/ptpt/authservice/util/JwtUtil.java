package com.ptpt.authservice.util;

import com.ptpt.authservice.dto.TempUserInfoDTO;
import com.ptpt.authservice.dto.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {
//    @Value("${jwt.secret}")
//    private String secret;
//
//    // 토큰에서 이메일 추출
//    public String extractEmail(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    // 토큰에서 만료일 추출
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    // 토큰에서 특정 클레임 추출
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // 모든 클레임 추출
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser().setSigningKey(secret).build().parseClaimsJws(token).getBody();
//    }
//
//    // 토큰 만료 여부 확인
//    private Boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    // 토큰 검증
//    public Boolean validateToken(String token, String email) {
//        final String extractedEmail = extractEmail(token);
//        return (extractedEmail.equals(email) && !isTokenExpired(token));
//    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 밀리초 단위, 예: 1800000 (30분)

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 밀리초 단위, 예: 604800000 (7일)

    @Value("${jwt.temp-token-expiration:1800000}") // 30분
    private long tempTokenExpiration;


    public String generateAccessToken(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return createToken(username, accessTokenExpiration);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return createToken(username, refreshTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), refreshTokenExpiration);
    }

    // 토큰 생성 공통 로직
    private String createToken(String subject, long expirationTime) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

//    public Claims parse(String token) {
//        return Jwts.parser()
//                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }

//    public String getEmailFromToken(String token) {
//        System.out.println(Jwts.parser()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject());
//
//
//        return Jwts.parser()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public boolean validateToken(String authToken) {
//        try {
//            Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(authToken);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            // 로그 또는 예외 처리
//        }
//        return false;
//    }

    // 토큰에서 모든 Claims 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 이메일(주제) 추출
    public String extractEmail(String token) {
        log.debug(token);
        log.debug(extractClaim(token, Claims::getSubject));

        return extractClaim(token, Claims::getSubject);
    }

    // 토큰에서 특정 Claim 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // 예외 발생 시 false
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String generateTempToken(TempUserInfoDTO tempUserInfo) {
        return Jwts.builder()
                .setSubject("TEMP_USER")
                .claim("email", tempUserInfo.getEmail())
                .claim("socialId", tempUserInfo.getSocialId())
                .claim("socialType", tempUserInfo.getSocialType().name())
                .claim("nickname", tempUserInfo.getNickname())
                .claim("profileImageUrl", tempUserInfo.getProfileImageUrl())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tempTokenExpiration))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public TempUserInfoDTO extractTempUserInfo(String tempToken) {
        Claims claims = extractAllClaims(tempToken);
        return TempUserInfoDTO.builder()
                .email(claims.get("email", String.class))
                .socialId(claims.get("socialId", String.class))
                .socialType(User.SocialType.valueOf(claims.get("socialType", String.class)))
                .nickname(claims.get("nickname", String.class))
                .profileImageUrl(claims.get("profileImageUrl", String.class))
                .build();
    }

    public boolean isTempToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "TEMP_USER".equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }
}
