package com.ptpt.authservice.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "API 응답 래퍼 클래스")
public class CustomApiResponse<T> {

    @Schema(description = "API 요청 성공 여부", example = "true")
    boolean success;

    @Schema(description = "응답 코드", example = "LOGIN_SUCCESS")
    String code;

    @Schema(description = "응답 메시지", example = "로그인이 성공적으로 완료되었습니다.")
    String message;

    @Schema(description = "응답 데이터", nullable = true)
    T data;
}
