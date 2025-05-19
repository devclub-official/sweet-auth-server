package com.ptpt.authservice.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API 오류 응답 스키마")
public class SwaggerErrorResponseDTO<T> {

    @Schema(description = "API 요청 성공 여부", example = "false")
    boolean success;

    @Schema(description = "응답 코드", example = "E0101")
    String code;

    @Schema(description = "응답 메시지", example = "예외 메시지 전송 or 로그인에 실패했습니다.")
    String message;

    @Schema(description = "응답 데이터", nullable = true)
    T data;

}
