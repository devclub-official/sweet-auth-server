package com.ptpt.authservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.apple.ApplePublicKey;
import com.ptpt.authservice.dto.apple.ApplePublicKeys;
import com.ptpt.authservice.service.SocialService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleServiceCopy implements SocialService {

    @Value("${apple.public-key-url:https://appleid.apple.com/auth/keys}")
    private String APPLE_PUBLIC_KEY_URL;

    @Value("${apple.iss:https://appleid.apple.com}")
    private String APPLE_ISS;

    @Value("${apple.client-id}")
    private String CLIENT_ID;

    private final ObjectMapper objectMapper;

    @Override
    public SocialUserInfo getUserInfo(String identityToken) {
        try {
            // 1. Apple의 공개키 가져오기
            ApplePublicKeys publicKeys = getApplePublicKeys();

            // 2. Identity Token 검증 및 파싱
            Claims claims = verifyAndParseToken(identityToken, publicKeys);

            // 3. SocialUserInfo 생성
            return SocialUserInfo.builder()
                    .socialId(claims.getSubject())  // sub claim이 Apple User ID
                    .email(claims.get("email", String.class))
                    .nickname(extractNickname(claims))
                    .profileImageUrl(null)  // Apple은 프로필 이미지를 제공하지 않음
                    .provider("APPLE")
                    .build();

        } catch (Exception e) {
            log.error("Apple 사용자 정보 조회 실패", e);
            throw new RuntimeException("Apple 사용자 정보 조회에 실패했습니다.", e);
        }
    }

    private ApplePublicKeys getApplePublicKeys() {
        return WebClient.create(APPLE_PUBLIC_KEY_URL)
                .get()
                .retrieve()
                .bodyToMono(ApplePublicKeys.class)
                .block();
    }

    private Claims verifyAndParseToken(String identityToken, ApplePublicKeys publicKeys) {
        try {
            // JWT의 헤더에서 kid 추출
            String[] chunks = identityToken.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
            Map<String, String> headerMap = objectMapper.readValue(header, Map.class);
            String kid = headerMap.get("kid");
            String alg = headerMap.get("alg");

            // kid에 해당하는 공개키 찾기
            ApplePublicKey publicKey = publicKeys.getKeys().stream()
                    .filter(key -> key.getKid().equals(kid))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("일치하는 공개키를 찾을 수 없습니다."));

            // RSA 공개키 생성
            PublicKey rsaPublicKey = createPublicKey(publicKey);

            // JWT 검증 및 파싱
            return Jwts.parser()
                    .setSigningKey(rsaPublicKey)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();

        } catch (Exception e) {
            log.error("Apple identity token 검증 실패", e);
            throw new RuntimeException("유효하지 않은 Apple identity token입니다.", e);
        }
    }

    private PublicKey createPublicKey(ApplePublicKey publicKey) throws Exception {
        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.getE());

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance(publicKey.getKty());

        return keyFactory.generatePublic(spec);
    }

    private String extractNickname(Claims claims) {
        // Apple은 이름 정보를 첫 로그인 시에만 제공할 수 있음
        String firstName = claims.get("firstName", String.class);
        String lastName = claims.get("lastName", String.class);

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            // 이름 정보가 없는 경우 이메일의 앞부분을 닉네임으로 사용
            String email = claims.get("email", String.class);
            if (email != null && email.contains("@")) {
                return email.split("@")[0];
            }
            return "Apple User";
        }
    }
}