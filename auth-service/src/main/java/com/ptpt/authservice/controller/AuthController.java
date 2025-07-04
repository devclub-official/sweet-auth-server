package com.ptpt.authservice.controller;

import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.request.RefreshTokenRequest;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.token.BlacklistedTokenException;
import com.ptpt.authservice.exception.token.InvalidTokenException;
import com.ptpt.authservice.service.AuthService;
import com.ptpt.authservice.service.JwtBlacklistService;
import com.ptpt.authservice.swagger.SwaggerAuthResponseDTO;
import com.ptpt.authservice.swagger.SwaggerErrorResponseDTO;
import com.ptpt.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "인증 API", description = "로그인 및 토큰 관리 API")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final JwtBlacklistService jwtBlacklistService;

    @Operation(
            summary = "로그인 API",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerAuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "로그인 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<TokenResponse>> login(@RequestBody LoginRequest loginRequest) {
//        try {
//            TokenResponse tokenResponse = authService.authenticateUser(loginRequest);
//
//            return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.AUTH_LOGIN_SUCCESS, tokenResponse));
//
//// https://ohju.tistory.com/405
//// 나는 로그를 그냥 쓰기만 했는데 올바른 로그 사용법이 따로 있었다.
////
//// log.info("data={}", name)
//// 이런 식으로 사용하면 된다고 한다.
////
//// log.info("data=" + name)
//// 내가 팀 프로젝트할 때 이렇게 사용한 것 같은데 이건 비추천하는 방식이라고 한다.
//// 왜냐하면 연산이 이루어지기 때문에 자원 낭비가 발생한다고 한다. 때문에 가능하면 위와 같은 방식으로 사용해야 할 것 같다.
//
////            로그레벨 순서
////            TRACE < DEBUG < INFO < WARN < ERROR < FATAL
////            log.info("{}", "log info 톄스트입니다.");
////            log.debug("{}", "log debug 톄스트입니다.");
////            log.warn("{}", "log warn 톄스트입니다.");
////            log.error("{}", "log error 톄스트입니다.");
////            log.trace("{}", "log trace 톄스트입니다.");
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    CustomApiResponse.of(ApiResponseCode.AUTH_LOGIN_FAILED, e.getMessage(), null));
//        }
        TokenResponse tokenResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.AUTH_LOGIN_SUCCESS, tokenResponse));
    }

    // Access Token 갱신을 위한 API
    @Operation(
            summary = "토큰 갱신 API",
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerAuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "토큰 갱신 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<CustomApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 블랙리스트에 Refresh Token이 있는지 확인
        if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
            throw new BlacklistedTokenException();
        }

        // Refresh Token이 유효한지 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidTokenException();
        }

        // Refresh Token에서 이메일 추출
        String email = jwtUtil.extractEmail(refreshToken);

        // 기존 Refresh Token을 블랙리스트에 추가
//            jwtBlacklistService.addToBlacklist(refreshToken);

        // 새로운 Access Token과 Refresh Token 발급
        TokenResponse tokenResponse = authService.refreshAccessToken(email);

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.AUTH_REFRESH_SUCCESS, tokenResponse));
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
