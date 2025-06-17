package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.response.SocialLoginResponse;
import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;
import com.ptpt.authservice.exception.social.SocialLoginFailedException;
import com.ptpt.authservice.exception.social.SocialPlatformException;
import com.ptpt.authservice.exception.social.SocialTokenInvalidException;
import com.ptpt.authservice.exception.user.UserCreateFailedException;
import com.ptpt.authservice.exception.user.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final SocialLoginService socialLoginService;
    private final Map<String, SocialService> socialServices;
    private final ObjectMapper objectMapper;

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
        log.info("소셜 회원가입 완료 요청 - nickname: {}", request.getNickname());

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

        // 5. 관심 스포츠를 JSON 문자열로 변환
        String interestedSportsJson = convertSportsListToJson(request.getInterestedSports());

        // 6. 사용자 생성
        User newUser = userService.createSocialUser(
                tempUserInfo.getEmail(),
                finalNickname,
                tempUserInfo.getSocialId(),
                tempUserInfo.getSocialType(),
                tempUserInfo.getProfileImageUrl(),
                request.getPhoneNumber(),
                request.getBirthDate(),
                request.getLocation(),
                interestedSportsJson,
                request.getProfileImageUrl(), // 사용자가 새로 업로드한 이미지
                request.getBio()
        );

        // 7. 토큰 발급
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

        // 생년월일 검증 (만 14세 이상, 120세 이하)
        if (request.getBirthDate() != null) {
            LocalDate now = LocalDate.now();
            int age = Period.between(request.getBirthDate(), now).getYears();

            if (age < 14) {
                throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "만 14세 이상만 가입할 수 있습니다.");
            }
            if (age > 120) {
                throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "올바르지 않은 생년월일입니다.");
            }
        }

        // 관심 스포츠 검증
        if (request.getInterestedSports() != null) {
            if (request.getInterestedSports().size() > 5) {
                throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "관심 스포츠는 최대 5개까지 선택할 수 있습니다.");
            }

            // 유효한 스포츠인지 검증
            List<String> validSports = getValidSportsList();
            for (String sport : request.getInterestedSports()) {
                if (!validSports.contains(sport)) {
                    throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "올바르지 않은 스포츠가 포함되어 있습니다: " + sport);
                }
            }
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
     * 관심 스포츠 목록을 JSON 문자열로 변환
     */
    private String convertSportsListToJson(List<String> sports) {
        try {
            return objectMapper.writeValueAsString(sports);
        } catch (JsonProcessingException e) {
            log.error("관심 스포츠 JSON 변환 실패", e);
            throw new AuthServiceException(ApiResponseCode.USER_CREATE_FAILED, "관심 스포츠 정보 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 유효한 스포츠 목록 반환
     */
    private List<String> getValidSportsList() {
        return List.of(
                "축구", "농구", "야구", "배구", "테니스", "탁구", "배드민턴", "골프",
                "수영", "육상", "체조", "태권도", "유도", "복싱", "레슬링", "펜싱",
                "양궁", "사격", "사이클", "스케이팅", "스키", "스노보드", "서핑", "요트",
                "클라이밍", "볼링", "당구", "다트", "E스포츠", "기타"
        );
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
}