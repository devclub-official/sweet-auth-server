package com.ptpt.authservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 응답 DTO")
public class TokenResponseDto {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;

    @Schema(description = "액세스 토큰 만료 시간(밀리초)", example = "3600")
    private long accessTokenExpiresIn;

    @Schema(description = "리프레시 토큰 만료 시간(밀리초)", example = "72000")
    private long refreshTokenExpiresIn;
}
