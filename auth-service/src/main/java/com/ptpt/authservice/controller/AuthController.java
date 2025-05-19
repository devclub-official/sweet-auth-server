package com.ptpt.authservice.controller;

import com.ptpt.authservice.controller.request.LoginRequest;
import com.ptpt.authservice.controller.request.RefreshTokenRequest;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.dto.response.TokenResponseDto;
import com.ptpt.authservice.service.AuthService;
import com.ptpt.authservice.service.JwtBlacklistService;
import com.ptpt.authservice.service.UserService;
import com.ptpt.authservice.swagger.SwaggerErrorResponseDTO;
import com.ptpt.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "인증 API", description = "로그인 및 토큰 관리 API")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;
    private final JwtBlacklistService jwtBlacklistService;

//    @PostMapping("/users")
//    public String auth(@RequestBody SimpleUserRequestBody requestBody) {
//        return userService.auth(requestBody.getEmail(), requestBody.getPassword());
//    }


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
                            schema = @Schema(implementation = TokenResponseDto.class)
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
    public ResponseEntity<CustomApiResponse<TokenResponseDto>> login(@RequestBody LoginRequest loginRequest) {
        try {
            TokenResponseDto tokenResponseDto = authService.authenticateUser(loginRequest);

            CustomApiResponse<TokenResponseDto> response = CustomApiResponse.<TokenResponseDto>builder()
                    .success(true)
                    .code("LOGIN_SUCCESS")
                    .message("로그인이 성공적으로 완료되었습니다.")
                    .data(tokenResponseDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CustomApiResponse<TokenResponseDto> errorResponse = CustomApiResponse.<TokenResponseDto>builder()
                    .success(false)
                    .code("LOGIN_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
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
                            schema = @Schema(implementation = TokenResponseDto.class)
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
    public ResponseEntity<CustomApiResponse<TokenResponseDto>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            // 블랙리스트에 Refresh Token이 있는지 확인
            if (jwtBlacklistService.isTokenBlacklisted(refreshToken)) {
                CustomApiResponse<TokenResponseDto> errorResponse = CustomApiResponse.<TokenResponseDto>builder()
                        .success(false)
                        .code("TOKEN_BLACKLISTED")
                        .message("해당 Refresh Token은 블랙리스트에 등록되어 있습니다.")
                        .data(null)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Refresh Token이 유효한지 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                CustomApiResponse<TokenResponseDto> errorResponse = CustomApiResponse.<TokenResponseDto>builder()
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

            CustomApiResponse<TokenResponseDto> response = CustomApiResponse.<TokenResponseDto>builder()
                    .success(true)
                    .code("TOKEN_REFRESH_SUCCESS")
                    .message("새로운 토큰이 성공적으로 발급되었습니다.")
                    .data(tokenResponseDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CustomApiResponse<TokenResponseDto> errorResponse = CustomApiResponse.<TokenResponseDto>builder()
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
