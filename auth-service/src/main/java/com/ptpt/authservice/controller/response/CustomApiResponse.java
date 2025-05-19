package com.ptpt.authservice.controller.response;

import com.ptpt.authservice.enums.ApiResponseCode;
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

    // ApiResponseCode Enum을 활용한 응답 생성 정적 메서드
    public static <T> CustomApiResponse<T> of(ApiResponseCode responseCode, T data) {
        return CustomApiResponse.<T>builder()
                .success(responseCode.isSuccess())
                .code(responseCode.getCode())
                .message(responseCode.getDefaultMessage())
                .data(data)
                .build();
    }

    // 메시지 커스터마이징을 위한 정적 메서드
    public static <T> CustomApiResponse<T> of(ApiResponseCode  responseCode, String customMessage, T data) {
        return CustomApiResponse.<T>builder()
                .success(responseCode.isSuccess())
                .code(responseCode.getCode())
                .message(customMessage)
                .data(data)
                .build();
    }
}
