package fast.campus.authservice.service;

import fast.campus.authservice.controller.request.LoginRequest;
import fast.campus.authservice.dto.response.TokenResponseDto;
import fast.campus.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 밀리초 단위, 예: 1800000 (30분)

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 밀리초 단위, 예: 604800000 (7일)

    public TokenResponseDto authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiration) // @Value 주입된 값 사용
                .refreshTokenExpiresIn(refreshTokenExpiration)
                .build();
    }
}
