package com.ptpt.authservice.swagger;

import com.ptpt.authservice.controller.response.TokenResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인증 API 응답 예시")
public class SwaggerAuthResponseDTO {

    @Schema(description = "API 요청 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 코드", example = "S0000")
    private String code;

    @Schema(description = "응답 메시지", example = "코드표 참조")
    private String message;

    @Schema(description = "응답 데이터")
    private TokenResponse data;
}
