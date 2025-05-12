package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.LoginRequest;
import fast.campus.authservice.controller.request.RefreshTokenRequest;
import fast.campus.authservice.controller.request.SimpleUserRequestBody;
import fast.campus.authservice.controller.response.ApiResponse;
import fast.campus.authservice.domain.User;
import fast.campus.authservice.dto.response.TokenResponseDto;
import fast.campus.authservice.service.AuthService;
import fast.campus.authservice.service.JwtBlacklistService;
import fast.campus.authservice.service.UserService;
import fast.campus.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;
    private final JwtBlacklistService jwtBlacklistService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpSec;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpSec;

//    @PostMapping("/users")
//    public String auth(@RequestBody SimpleUserRequestBody requestBody) {
//        return userService.auth(requestBody.getEmail(), requestBody.getPassword());
//    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@RequestBody LoginRequest loginRequest) {
        try {
            TokenResponseDto tokenResponseDto = authService.authenticateUser(loginRequest);

            ApiResponse<TokenResponseDto> response = ApiResponse.<TokenResponseDto>builder()
                    .success(true)
                    .code("LOGIN_SUCCESS")
                    .message("로그인이 성공적으로 완료되었습니다.")
                    .data(tokenResponseDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
                    .success(false)
                    .code("LOGIN_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Access Token 갱신을 위한 API
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 블랙리스트에 Refresh Token이 있는지 확인
            if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
                ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
                        .success(false)
                        .code("TOKEN_BLACKLISTED")
                        .message("해당 Refresh Token은 블랙리스트에 등록되어 있습니다.")
                        .data(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Refresh Token이 유효한지 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
                        .success(false)
                        .code("INVALID_REFRESH_TOKEN")
                        .message("유효하지 않은 Refresh Token입니다.")
                        .data(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Refresh Token에서 이메일 추출
            String email = jwtUtil.extractEmail(refreshToken);

            // 기존 Refresh Token을 블랙리스트에 추가
//            jwtBlacklistService.addToBlacklist(refreshToken);

            // 새로운 Access Token과 Refresh Token 발급
            TokenResponseDto tokenResponseDto = authService.refreshAccessToken(email);

            ApiResponse<TokenResponseDto> response = ApiResponse.<TokenResponseDto>builder()
                    .success(true)
                    .code("TOKEN_REFRESH_SUCCESS")
                    .message("새로운 토큰이 성공적으로 발급되었습니다.")
                    .data(tokenResponseDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
                    .success(false)
                    .code("TOKEN_REFRESH_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Access Token 갱신을 위한 API
//    @PostMapping("/token/refresh")
//    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(@RequestBody String refreshToken) {
//        try {
//            // 블랙리스트에 Refresh Token이 있는지 확인
//            if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
//                ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
//                        .success(false)
//                        .code("TOKEN_BLACKLISTED")
//                        .message("해당 Refresh Token은 블랙리스트에 등록되어 있습니다.")
//                        .data(null)
//                        .build();
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//
//            // Refresh Token이 유효한지 검증
//            if (!jwtUtil.validateToken(refreshToken)) {
//                ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
//                        .success(false)
//                        .code("INVALID_REFRESH_TOKEN")
//                        .message("유효하지 않은 Refresh Token입니다.")
//                        .data(null)
//                        .build();
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//
//            // Refresh Token을 사용하여 새로운 Access Token 생성
//            String email = jwtUtil.getEmailFromToken(refreshToken);  // Refresh Token에서 이메일 추출
//            TokenResponseDto tokenResponseDto = authService.refreshAccessToken(email);
//
//            ApiResponse<TokenResponseDto> response = ApiResponse.<TokenResponseDto>builder()
//                    .success(true)
//                    .code("TOKEN_REFRESH_SUCCESS")
//                    .message("새로운 토큰이 성공적으로 발급되었습니다.")
//                    .data(tokenResponseDto)
//                    .build();
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
//                    .success(false)
//                    .code("TOKEN_REFRESH_FAILED")
//                    .message(e.getMessage())
//                    .data(null)
//                    .build();
//            return ResponseEntity.badRequest().body(errorResponse);
//        }
//    }
}
