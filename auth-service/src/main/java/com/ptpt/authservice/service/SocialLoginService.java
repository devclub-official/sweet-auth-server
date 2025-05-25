package com.ptpt.authservice.service;

import com.ptpt.authservice.dto.SocialLoginResponse;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * 소셜 로그인 프로세스 처리
     */
    public SocialLoginResponse processSocialLogin(SocialUserInfo socialUserInfo) {
        // 1. 기존 소셜 사용자 확인
        Optional<User> existingSocialUser = userService.findBySocialInfo(
                socialUserInfo.getSocialId(),
                User.SocialType.valueOf(socialUserInfo.getProvider())
        );

        if (existingSocialUser.isPresent()) {
            return processExistingUser(existingSocialUser.get());
        }

        // 2. 이메일 중복 확인
        checkEmailDuplication(socialUserInfo.getEmail());

        // 3. 신규 사용자 처리
        return processNewUser(socialUserInfo);
    }

    /**
     * 기존 사용자 로그인 처리
     */
    private SocialLoginResponse processExistingUser(User user) {
        log.info("기존 소셜 사용자 로그인 - userId: {}", user.getId());

        return SocialLoginResponse.builder()
                .status("LOGIN_SUCCESS")
                .tokens(tokenService.generateTokens(user))
                .build();
    }

    /**
     * 신규 사용자 회원가입 필요 응답 생성
     */
    private SocialLoginResponse processNewUser(SocialUserInfo socialUserInfo) {
        log.info("신규 소셜 사용자 - 회원가입 필요");

        TempUserInfo tempUserInfo = buildTempUserInfo(socialUserInfo);
        String tempToken = tokenService.generateTempToken(tempUserInfo);

        return SocialLoginResponse.builder()
                .status("SIGNUP_REQUIRED")
                .tempToken(tempToken)
                .tempUserInfo(tempUserInfo)
                .requiredFields(Arrays.asList("phoneNumber", "agreeTerms"))
                .build();
    }

    /**
     * 이메일 중복 확인
     */
    private void checkEmailDuplication(String email) {
        Optional<User> existingEmailUser = userService.findByEmail(email);

        if (existingEmailUser.isPresent() && existingEmailUser.get().isNormalUser()) {
            throw new AuthException("해당 이메일로 이미 일반 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
        }
    }

    /**
     * 임시 사용자 정보 생성
     */
    private TempUserInfo buildTempUserInfo(SocialUserInfo socialUserInfo) {
        return TempUserInfo.builder()
                .email(socialUserInfo.getEmail())
                .socialId(socialUserInfo.getSocialId())
                .socialType(User.SocialType.valueOf(socialUserInfo.getProvider()))
                .nickname(socialUserInfo.getNickname())
                .profileImageUrl(socialUserInfo.getProfileImageUrl())
                .build();
    }
}
