package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.SocialLoginResponse;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.exception.AuthException;
import com.ptpt.authservice.exception.DuplicateException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final SocialLoginService socialLoginService;
    private final Map<String, SocialService> socialServices;

    /**
     * 일반 로그인 처리
     */
    @Transactional
    public TokenResponse authenticateUser(LoginRequest loginRequest) {
        log.info("일반 로그인 시도 - email: {}", loginRequest.getEmail());

        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        return tokenService.generateTokens(user);
    }

    /**
     * 리프레시 토큰으로 새 토큰 발급
     */
    @Transactional
    public TokenResponse refreshAccessToken(String email) {
        log.info("토큰 갱신 요청 - email: {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AuthException("사용자를 찾을 수 없습니다."));

        return tokenService.generateTokens(user);
    }

    /**
     * 소셜 로그인 처리
     */
    @Transactional
    public SocialLoginResponse handleSocialLogin(String provider, String accessToken) {
        log.info("소셜 로그인 시도 - provider: {}", provider);

        // 1. 소셜 사용자 정보 조회
        SocialUserInfo socialUserInfo = getSocialUserInfo(provider, accessToken);

        // 2. 기존 사용자 확인 및 처리
        return socialLoginService.processSocialLogin(socialUserInfo);
    }

    /**
     * 소셜 회원가입 완료
     */
    @Transactional
    public TokenResponse completeSocialSignup(String tempToken, CompleteSignupRequest request) {
        log.info("소셜 회원가입 완료 요청");

        // 1. 임시 토큰 검증 및 정보 추출
        TempUserInfo tempUserInfo = tokenService.validateAndExtractTempToken(tempToken);

        // 2. 닉네임 검증
        String finalNickname = validateAndGetFinalNickname(tempUserInfo, request.getNickname());

        // 3. 사용자 생성
        User newUser = userService.createSocialUser(
                tempUserInfo.getEmail(),
                finalNickname,
                tempUserInfo.getSocialId(),
                tempUserInfo.getSocialType(),
                tempUserInfo.getProfileImageUrl(),
                request.getPhoneNumber()
        );

        // 4. 토큰 발급
        return tokenService.generateTokens(newUser);
    }

    /**
     * 소셜 서비스에서 사용자 정보 조회
     */
    private SocialUserInfo getSocialUserInfo(String provider, String accessToken) {
        String serviceKey = provider.toLowerCase() + "Service";
        SocialService socialService = socialServices.get(serviceKey);

        if (socialService == null) {
            throw new AuthException("지원하지 않는 소셜 플랫폼입니다: " + provider);
        }

        try {
            return socialService.getUserInfo(accessToken);
        } catch (Exception e) {
            log.error("소셜 사용자 정보 조회 실패 - provider: {}", provider, e);
            throw new AuthException("소셜 플랫폼에서 사용자 정보를 가져올 수 없습니다.");
        }
    }

    /**
     * 닉네임 검증 및 최종 닉네임 반환
     */
    private String validateAndGetFinalNickname(TempUserInfo tempUserInfo, String requestedNickname) {
        String finalNickname = requestedNickname != null ? requestedNickname : tempUserInfo.getNickname();

        if (!finalNickname.equals(tempUserInfo.getNickname())) {
            if (userService.existsByNickname(finalNickname)) {
                throw new DuplicateException("이미 존재하는 닉네임입니다");
            }
        }

        return finalNickname;
    }

//    public SocialLoginResponse handleKakaoLogin(KakaoUserInfoResponseDTO kakaoUserInfo) {
//        String email = kakaoUserInfo.getKakaoAccount().getEmail();
//        String socialId = String.valueOf(kakaoUserInfo.getId());
//
//        // 기존 소셜 사용자인지 확인
//        Optional<User> existingSocialUser = userService.findBySocialInfo(socialId, User.SocialType.KAKAO);
//        if (existingSocialUser.isPresent()) {
//            // 기존 소셜 사용자 로그인 처리
//            TokenResponseDTO tokens = generateTokensForUser(existingSocialUser.get());
//            return SocialLoginResponse.builder()
//                    .status("LOGIN_SUCCESS")
//                    .tokens(tokens)
//                    .build();
//        }
//
//        // 이메일로 일반 가입된 사용자인지 확인
//        Optional<User> existingEmailUser = userService.findByEmail(email);
//        if (existingEmailUser.isPresent() && existingEmailUser.get().isNormalUser()) {
//            throw new IllegalArgumentException("해당 이메일로 이미 일반 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
//        }
//
//        // 신규 사용자 - 임시 토큰 발급
//        TempUserInfo tempUserInfo = TempUserInfo.builder()
//                .email(email)
//                .socialId(socialId)
//                .socialType(User.SocialType.KAKAO)
//                .nickname(kakaoUserInfo.getKakaoAccount().getProfile().getNickName())
//                .profileImageUrl(kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl())
//                .build();
//
//        String tempToken = jwtUtil.generateTempToken(tempUserInfo);
//
//        return SocialLoginResponse.builder()
//                .status("SIGNUP_REQUIRED")
//                .tempToken(tempToken)
//                .tempUserInfo(tempUserInfo)
//                .requiredFields(Arrays.asList("phoneNumber", "agreeTerms"))
//                .build();
//    }
//
//    public TokenResponseDTO completeSocialSignup(String tempToken, CompleteSignupRequest request) {
//        // 임시 토큰 검증
//        if (!jwtUtil.validateToken(tempToken) || !jwtUtil.isTempToken(tempToken)) {
//            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다");
//        }
//
//        // 임시 사용자 정보 추출
//        TempUserInfo tempUserInfo = jwtUtil.extractTempUserInfo(tempToken);
//
//        // 닉네임 중복 확인 (변경된 경우)
//        String finalNickname = request.getNickname() != null ? request.getNickname() : tempUserInfo.getNickname();
//        if (!finalNickname.equals(tempUserInfo.getNickname()) && userRepository.existsByNickname(finalNickname)) {
//            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
//        }
//
//        // 실제 사용자 생성
//        User newUser = userService.createSocialUser(
//                tempUserInfo.getEmail(),
//                finalNickname,
//                tempUserInfo.getSocialId(),
//                tempUserInfo.getSocialType(),
//                tempUserInfo.getProfileImageUrl(),
//                request.getPhoneNumber()
//        );
//
//        // 정식 토큰 발급
//        return generateTokensForUser(newUser);
//    }
//
//    private TokenResponseDTO generateTokensForUser(User user) {
//        String accessToken = jwtUtil.generateAccessToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
//
//        return TokenResponseDTO.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
}
