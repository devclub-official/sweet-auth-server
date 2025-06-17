package com.ptpt.authservice.service.impl;

import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.kakao.KakaoUserInfoResponse;
import com.ptpt.authservice.exception.social.SocialPlatformException;
import com.ptpt.authservice.exception.social.SocialTokenInvalidException;
import com.ptpt.authservice.service.SocialService;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService implements SocialService {

    @Value("${kakao.kauth-user-url}")
    private String KAUTH_USER_URL_HOST;

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        KakaoUserInfoResponse kakaoUserInfo = getKakaoUserInfo(accessToken);

        log.info("[ Kakao Service ] Auth ID ---> {} ", kakaoUserInfo.getId());

        return SocialUserInfo.builder()
                .socialId(String.valueOf(kakaoUserInfo.getId()))
                .email(kakaoUserInfo.getKakaoAccount().getEmail())
                .nickname(kakaoUserInfo.getKakaoAccount().getProfile().getNickName())
                .profileImageUrl(kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                .provider("KAKAO")
                .build();
    }

    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {

        try {
            log.info("KAUTH_USER_URL_HOST = {}", KAUTH_USER_URL_HOST);
            log.info("[ Kakao Service ] Access Token ---> {} ", accessToken);

            KakaoUserInfoResponse userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .path("/v2/user/me")
                            .build(true))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            clientResponse -> {
                                log.error("카카오 API 4xx 에러 발생: {}", clientResponse.statusCode());
                                if (clientResponse.statusCode().value() == 401) {
                                    return Mono.error(new SocialTokenInvalidException("유효하지 않은 카카오 액세스 토큰입니다."));
                                }
                                return Mono.error(new SocialPlatformException("카카오 API 요청이 잘못되었습니다."));
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> {
                                log.error("5xx 에러 발생: {}", clientResponse.statusCode());
                                return Mono.error(new SocialPlatformException("카카오 서버에서 오류가 발생했습니다."));
                            })
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();

            log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
            log.info("[ Kakao Service ] Email ---> {} ", userInfo.getKakaoAccount().getEmail());
            log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());

            return userInfo;
        } catch (SocialTokenInvalidException | SocialPlatformException e) {
            // 이미 커스텀 예외인 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("[Kakao Service] 유저 정보 요청 중 예외 발생", e);
            throw new SocialPlatformException("카카오 사용자 정보 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }
}