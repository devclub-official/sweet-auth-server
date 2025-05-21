package com.ptpt.authservice.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API 오류 응답 스키마")
public class SwaggerErrorResponseDTO<T> {

    @Schema(description = "API 요청 성공 여부", example = "false")
    boolean success;

    @Schema(description = "응답 코드", example = "E0000")
    String code;

    @Schema(description = "응답 메시지", example = "예외 메시지 전송 or 커스텀 메시지")
    String message;

    @Schema(description = "응답 데이터", example = "null", defaultValue = "null")
    T data;

}
