package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.response.TokenResponseDTO;
import com.ptpt.authservice.dto.KakaoUserInfoResponseDTO;
import com.ptpt.authservice.dto.SocialLoginResponseDTO;
import com.ptpt.authservice.dto.TempUserInfoDTO;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.entity.user.UserEntity;
import com.ptpt.authservice.repository.user.UserJpaRepository;
import com.ptpt.authservice.repository.user.UserRepository;
import com.ptpt.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 밀리초 단위, 예: 1800000 (30분)

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 밀리초 단위, 예: 604800000 (7일)

    public TokenResponseDTO authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiration) // @Value 주입된 값 사용
                .refreshTokenExpiresIn(refreshTokenExpiration)
                .build();
    }

    // Refresh Token을 사용하여 새로운 Access Token과 Refresh Token 발급
    public TokenResponseDTO refreshAccessToken(String email) {
        // 기존 리프레시 토큰을 블랙리스트에 추가하는 로직은 컨트롤러에서 처리

        // 사용자를 이메일로 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 새로운 Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiration)
                .refreshTokenExpiresIn(refreshTokenExpiration)
                .build();
    }

    public SocialLoginResponseDTO handleKakaoLogin(KakaoUserInfoResponseDTO kakaoUserInfo) {
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        String socialId = String.valueOf(kakaoUserInfo.getId());

        // 기존 소셜 사용자인지 확인
        Optional<User> existingSocialUser = userService.findBySocialInfo(socialId, User.SocialType.KAKAO);
        if (existingSocialUser.isPresent()) {
            // 기존 소셜 사용자 로그인 처리
            TokenResponseDTO tokens = generateTokensForUser(existingSocialUser.get());
            return SocialLoginResponseDTO.builder()
                    .status("LOGIN_SUCCESS")
                    .tokens(tokens)
                    .build();
        }

        // 이메일로 일반 가입된 사용자인지 확인
        Optional<User> existingEmailUser = userService.findByEmail(email);
        if (existingEmailUser.isPresent() && existingEmailUser.get().isNormalUser()) {
            throw new IllegalArgumentException("해당 이메일로 이미 일반 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.");
        }

        // 신규 사용자 - 임시 토큰 발급
        TempUserInfoDTO tempUserInfo = TempUserInfoDTO.builder()
                .email(email)
                .socialId(socialId)
                .socialType(User.SocialType.KAKAO)
                .nickname(kakaoUserInfo.getKakaoAccount().getProfile().getNickName())
                .profileImageUrl(kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                .build();

        String tempToken = jwtUtil.generateTempToken(tempUserInfo);

        return SocialLoginResponseDTO.builder()
                .status("SIGNUP_REQUIRED")
                .tempToken(tempToken)
                .tempUserInfo(tempUserInfo)
                .requiredFields(Arrays.asList("phoneNumber", "agreeTerms"))
                .build();
    }

    public TokenResponseDTO completeSocialSignup(String tempToken, CompleteSignupRequest request) {
        // 임시 토큰 검증
        if (!jwtUtil.validateToken(tempToken) || !jwtUtil.isTempToken(tempToken)) {
            throw new IllegalArgumentException("유효하지 않은 임시 토큰입니다");
        }

        // 임시 사용자 정보 추출
        TempUserInfoDTO tempUserInfo = jwtUtil.extractTempUserInfo(tempToken);

        // 닉네임 중복 확인 (변경된 경우)
        String finalNickname = request.getNickname() != null ? request.getNickname() : tempUserInfo.getNickname();
        if (!finalNickname.equals(tempUserInfo.getNickname()) && userRepository.existsByNickname(finalNickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }

        // 실제 사용자 생성
        User newUser = userService.createSocialUser(
                tempUserInfo.getEmail(),
                finalNickname,
                tempUserInfo.getSocialId(),
                tempUserInfo.getSocialType(),
                tempUserInfo.getProfileImageUrl(),
                request.getPhoneNumber()
        );

        // 정식 토큰 발급
        return generateTokensForUser(newUser);
    }

    private TokenResponseDTO generateTokensForUser(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
