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

    String code;

    String message;

    T data;
}
