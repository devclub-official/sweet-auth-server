package com.ptpt.authservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.apple.ApplePublicKey;
import com.ptpt.authservice.dto.apple.ApplePublicKeys;
import com.ptpt.authservice.exception.social.SocialPlatformException;
import com.ptpt.authservice.exception.social.SocialTokenInvalidException;
import com.ptpt.authservice.service.SocialService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleService implements SocialService {

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
            log.info("[ Apple Service ] Identity Token 검증 시작");
            log.info("[ Apple Service ] Identity Token ---> {}", identityToken.substring(0, Math.min(50, identityToken.length())) + "...");

            // 1. Apple의 공개키 가져오기
            ApplePublicKeys publicKeys = getApplePublicKeys();
            log.info("[ Apple Service ] Apple 공개키 조회 완료");

            // 2. Identity Token 검증 및 파싱
            Claims claims = verifyAndParseToken(identityToken, publicKeys);
            log.info("[ Apple Service ] Identity Token 검증 완료");

            // 3. Claims에서 사용자 정보 추출
            String socialId = claims.getSubject();
            String email = claims.get("email", String.class);
            String nickname = extractNickname(claims);

            log.info("[ Apple Service ] Apple ID ---> {}", socialId);
            log.info("[ Apple Service ] Email ---> {}", email);
            log.info("[ Apple Service ] NickName ---> {}", nickname);

            // 4. SocialUserInfo 생성
            return SocialUserInfo.builder()
                    .socialId(socialId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(null)  // Apple은 프로필 이미지를 제공하지 않음
                    .provider("APPLE")
                    .build();

        }  catch (SocialTokenInvalidException | SocialPlatformException e) {
            // 이미 커스텀 예외인 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("[ Apple Service ] 사용자 정보 조회 중 예외 발생", e);
            throw new SocialPlatformException("Apple 사용자 정보 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    private ApplePublicKeys getApplePublicKeys() {
        try {
            log.info("APPLE_PUBLIC_KEY_URL = {}", APPLE_PUBLIC_KEY_URL);

            return WebClient.create(APPLE_PUBLIC_KEY_URL)
                    .get()
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> {
                                log.error("Apple 공개키 조회 4xx 에러 발생: {}", clientResponse.statusCode());
                                return Mono.error(new SocialPlatformException("Apple 공개키 조회 요청이 잘못되었습니다."));
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> {
                                log.error("Apple 공개키 조회 5xx 에러 발생: {}", clientResponse.statusCode());
                                return Mono.error(new SocialPlatformException("Apple 서버에서 오류가 발생했습니다."));
                            })
                    .bodyToMono(ApplePublicKeys.class)
                    .block();

        } catch (SocialPlatformException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ Apple Service ] 공개키 조회 중 예외 발생", e);
            throw new SocialPlatformException("Apple 공개키 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    private Claims verifyAndParseToken(String identityToken, ApplePublicKeys publicKeys) {
        try {
            log.debug("identityToken = {}", identityToken.substring(0, Math.min(50, identityToken.length())) + "...");

            // JWT의 헤더에서 kid 추출
            String[] chunks = identityToken.split("\\.");
            if (chunks.length != 3) {
                throw new SocialTokenInvalidException("올바르지 않은 JWT 형식입니다.");
            }

            String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
            Map<String, String> headerMap = objectMapper.readValue(header, Map.class);
            String kid = headerMap.get("kid");
            String alg = headerMap.get("alg");

            log.info("[ Apple Service ] JWT Header - kid: {}, alg: {}", kid, alg);

            // kid에 해당하는 공개키 찾기
            ApplePublicKey publicKey = publicKeys.getKeys().stream()
                    .filter(key -> key.getKid().equals(kid))
                    .findFirst()
                    .orElseThrow(() -> new SocialTokenInvalidException("일치하는 공개키를 찾을 수 없습니다. kid: " + kid));

            log.info("[ Apple Service ] 공개키 매칭 완료 - kid: {}", kid);

            // RSA 공개키 생성
            PublicKey rsaPublicKey = createPublicKey(publicKey);

            // JWT 검증 및 파싱
            Claims claims = Jwts.parser()
                    .setSigningKey(rsaPublicKey)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();

            // 추가 검증 (iss, aud 등)
            validateClaims(claims);

            return claims;

        }  catch (SocialTokenInvalidException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ Apple Service ] Identity token 검증 실패", e);
            throw new SocialTokenInvalidException("유효하지 않은 Apple identity token입니다: " + e.getMessage());
        }
    }

    private void validateClaims(Claims claims) {
        // iss 검증 (토큰 발급자)
        String iss = claims.getIssuer();
        if (!APPLE_ISS.equals(iss)) {
            throw new SocialTokenInvalidException("유효하지 않은 issuer입니다. expected: " + APPLE_ISS + ", actual: " + iss);
        }

        // aud 검증 (클라이언트 ID 확인) - Apple은 단일 audience 사용
        Set<String> aud = claims.getAudience();
        if (aud == null || aud.isEmpty() || !aud.contains(CLIENT_ID)) {
            throw new SocialTokenInvalidException("유효하지 않은 audience입니다. expected: " + CLIENT_ID + ", actual: " + aud);
        }

        // exp 검증 (만료 시간) - JJWT 라이브러리가 자동으로 검증하지만 명시적으로 확인
        if (claims.getExpiration() != null && claims.getExpiration().getTime() < System.currentTimeMillis()) {
            throw new SocialTokenInvalidException("만료된 토큰입니다.");
        }

        // sub 검증 (사용자 식별자)
        String sub = claims.getSubject();
        if (sub == null || sub.trim().isEmpty()) {
            throw new SocialTokenInvalidException("유효하지 않은 사용자 식별자입니다.");
        }

//        log.info("[ Apple Service ] Claims 검증 완료 - iss: {}, aud: {}, sub: {}", iss, aud, sub);
        log.info("[ Apple Service ] Token 만료시간: {}", claims.getExpiration());
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