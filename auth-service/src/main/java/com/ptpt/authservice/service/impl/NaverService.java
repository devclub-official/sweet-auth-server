//package com.ptpt.authservice.service;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.ptpt.authservice.dto.SocialUserInfo;
//import com.ptpt.authservice.dto.naver.NaverUserInfo;
//import com.ptpt.authservice.dto.naver.NaverUserInfoResponse;
//import com.ptpt.authservice.exceptions.social.SocialPlatformException;
//import com.ptpt.authservice.exceptions.social.SocialTokenInvalidException;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
///**
// * 네이버 소셜 로그인 서비스
// * 프론트엔드에서 받은 네이버 액세스 토큰으로 사용자 정보를 조회합니다.
// */
//@Slf4j
//@Service("naverService")
//@RequiredArgsConstructor
//public class NaverService implements SocialService {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${social.naver.user-info-uri:https://openapi.naver.com/v1/nid/me}")
//    private String naverUserInfoUri;
//
//    @Override
//    public SocialUserInfo getUserInfo(String accessToken) {
//        log.info("네이버 사용자 정보 조회 시작");
//
//        try {
//            // HTTP 헤더 설정
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", "Bearer " + accessToken);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            // 네이버 API 호출
//            ResponseEntity<NaverUserInfoResponse> response = restTemplate.exchange(
//                    naverUserInfoUri,
//                    HttpMethod.GET,
//                    entity,
//                    NaverUserInfoResponse.class
//            );
//
//            NaverUserInfoResponse naverResponse = response.getBody();
//
//            if (naverResponse == null || naverResponse.getResponse() == null) {
//                throw new SocialPlatformException("네이버에서 사용자 정보를 가져올 수 없습니다.");
//            }
//
//            if (!"00".equals(naverResponse.getResultcode())) {
//                log.warn("네이버 API 오류 - resultcode: {}, message: {}",
//                        naverResponse.getResultcode(), naverResponse.getMessage());
//                throw new SocialPlatformException("네이버 API 호출 실패: " + naverResponse.getMessage());
//            }
//
//            NaverUserInfo naverUserInfo = naverResponse.getResponse();
//
//            // 필수 정보 검증
//            if (naverUserInfo.getEmail() == null || naverUserInfo.getEmail().isEmpty()) {
//                throw new SocialPlatformException("네이버 계정에 이메일 정보가 없습니다.");
//            }
//
//            // SocialUserInfo로 변환
//            SocialUserInfo socialUserInfo = SocialUserInfo.builder()
//                    .socialId(naverUserInfo.getId())
//                    .email(naverUserInfo.getEmail())
//                    .nickname(generateNickname(naverUserInfo))
//                    .profileImageUrl(naverUserInfo.getProfileImageUrl())
//                    .provider("NAVER")
//                    .build();
//
//            log.info("네이버 사용자 정보 조회 완료 - email: {}", socialUserInfo.getEmail());
//            return socialUserInfo;
//
//        } catch (HttpClientErrorException e) {
//            log.error("네이버 API 호출 HTTP 오류 - status: {}, body: {}",
//                    e.getStatusCode(), e.getResponseBodyAsString());
//
//            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//                throw new SocialTokenInvalidException("네이버 액세스 토큰이 유효하지 않습니다.");
//            }
//
//            throw new SocialPlatformException("네이버 API 호출 중 오류가 발생했습니다: " + e.getMessage());
//
//        } catch (SocialTokenInvalidException | SocialPlatformException e) {
//            // 이미 적절한 예외인 경우 그대로 던짐
//            throw e;
//
//        } catch (Exception e) {
//            log.error("네이버 사용자 정보 조회 중 예상치 못한 오류", e);
//            throw new SocialPlatformException("네이버 사용자 정보 조회 중 오류가 발생했습니다.");
//        }
//    }
//
//    /**
//     * 닉네임 생성 로직
//     * 네이버에서 제공하는 정보 중 사용 가능한 것으로 닉네임 생성
//     */
//    private String generateNickname(NaverUserInfo naverUserInfo) {
//        // 1. 닉네임이 있으면 사용
//        if (naverUserInfo.getNickname() != null && !naverUserInfo.getNickname().isEmpty()) {
//            return naverUserInfo.getNickname();
//        }
//
//        // 2. 이름이 있으면 사용
//        if (naverUserInfo.getName() != null && !naverUserInfo.getName().isEmpty()) {
//            return naverUserInfo.getName();
//        }
//
//        // 3. 이메일 앞부분 사용
//        String email = naverUserInfo.getEmail();
//        if (email != null && email.contains("@")) {
//            return email.substring(0, email.indexOf("@"));
//        }
//
//        // 4. 기본값
//        return "네이버사용자" + System.currentTimeMillis() % 10000;
//    }
//}
