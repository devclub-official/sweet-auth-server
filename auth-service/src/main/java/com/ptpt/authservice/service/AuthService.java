package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.response.SocialLoginResponse;
import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;
import com.ptpt.authservice.exceptions.social.SocialLoginFailedException;
import com.ptpt.authservice.exceptions.social.SocialPlatformException;
import com.ptpt.authservice.exceptions.social.SocialTokenInvalidException;
import com.ptpt.authservice.exceptions.user.UserCreateFailedException;
import com.ptpt.authservice.exceptions.user.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .orElseThrow(() -> new UserNotFoundException("토큰 갱신을 위한 사용자를 찾을 수 없습니다: " + email));

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

        // 1. 요청 데이터 검증
        try {
            request.validateRequiredFields();
        } catch (IllegalArgumentException e) {
            throw new UserCreateFailedException(e.getMessage());
        }

        // 2. 임시 토큰 검증 및 정보 추출
        TempUserInfo tempUserInfo = tokenService.validateAndExtractTempToken(tempToken);

        // 3. 닉네임 검증
        String finalNickname = validateAndGetFinalNickname(tempUserInfo, request.getNickname());

        // 4. 추가 필드 검증
        validateAdditionalFields(request);

        // 5. 사용자 생성
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
     * 추가 필드 검증
     */
    private void validateAdditionalFields(CompleteSignupRequest request) {
        // 전화번호 형식 검증
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (!isValidPhoneNumber(request.getPhoneNumber())) {
                throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "올바르지 않은 전화번호 형식입니다.");
            }
        }

        // Bio 길이 제한
        if (request.getBio() != null && request.getBio().length() > 500) {
            throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "자기소개는 500자를 초과할 수 없습니다.");
        }
    }

    /**
     * 전화번호 형식 검증
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // 간단한 한국 전화번호 형식 검증
        String phoneRegex = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$";
        return phoneNumber.matches(phoneRegex);
    }

    /**
     * 소셜 서비스에서 사용자 정보 조회
     */
    private SocialUserInfo getSocialUserInfo(String provider, String accessToken) {
        String serviceKey = provider.toLowerCase() + "Service";
        SocialService socialService = socialServices.get(serviceKey);

        if (socialService == null) {
            throw new SocialLoginFailedException("지원하지 않는 소셜 플랫폼입니다: " + provider);
        }

        try {
            return socialService.getUserInfo(accessToken);
        } catch (SocialTokenInvalidException | SocialPlatformException e) {
            // 이미 적절한 예외인 경우 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("소셜 사용자 정보 조회 실패 - provider: {}", provider, e);
            throw new SocialPlatformException("소셜 플랫폼에서 사용자 정보를 가져올 수 없습니다.");
        }
    }

    /**
     * 닉네임 검증 및 최종 닉네임 반환
     */
    private String validateAndGetFinalNickname(TempUserInfo tempUserInfo, String requestedNickname) {
        String finalNickname = requestedNickname != null ? requestedNickname : tempUserInfo.getNickname();

        // 기존 닉네임과 다른 경우에만 중복 검사
        if (!finalNickname.equals(tempUserInfo.getNickname())) {
            if (userService.existsByNickname(finalNickname)) {
                throw new UserCreateFailedException("이미 존재하는 닉네임입니다: " + finalNickname);
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
