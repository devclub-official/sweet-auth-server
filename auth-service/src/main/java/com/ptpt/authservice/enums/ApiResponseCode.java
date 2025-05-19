package com.ptpt.authservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiResponseCode {
    // 인증 관련 성공 코드
    AUTH_LOGIN_SUCCESS("S0101", "로그인이 성공적으로 완료되었습니다."),
    AUTH_REFRESH_SUCCESS("S0102", "토큰이 성공적으로 갱신되었습니다."),

    // 인증 관련 실패 코드
    AUTH_LOGIN_FAILED("E0101", "로그인에 실패했습니다."),
    AUTH_REFRESH_FAILED("E0102", "토큰 갱신에 실패했습니다."),
    AUTH_TOKEN_BLACKLISTED("E0103", "해당 토큰은 블랙리스트에 등록되어 사용이 중지되었습니다."),
    AUTH_TOKEN_EXPIRED("E0104", "토큰이 만료되었습니다."),
    AUTH_TOKEN_MALFORMED("E0105", "토큰 형식이 올바르지 않습니다."),
    AUTH_TOKEN_MISSING("E0106", "인증 토큰이 누락되었습니다."),
    AUTH_TOKEN_UNSUPPORTED("E0107", "지원되지 않는 토큰 형식입니다."),
    AUTH_TOKEN_INVALID_SIGNATURE("E0108", "토큰 서명이 유효하지 않습니다."),
    AUTH_TOKEN_INVALID("E0109", "유효하지 않은 토큰입니다."),

    // HTTP 상태 코드 관련 오류
    // 인증/인가 관련 오류 코드
    AUTH_UNAUTHORIZED("E0401", "인증에 실패했습니다. 유효한 자격 증명이 필요합니다."),
    AUTH_FORBIDDEN("E0409", "이 리소스에 대한 접근 권한이 없습니다."),

    // 사용자 관련 성공 코드
    USER_CREATE_SUCCESS("S0203", "사용자가 성공적으로 생성되었습니다."),
    USER_READ_SUCCESS("S0204", "사용자 정보가 성공적으로 조회되었습니다."),
    USER_UPDATE_SUCCESS("S0205", "사용자 정보가 성공적으로 업데이트되었습니다."),
    USER_DELETE_SUCCESS("S0206", "사용자가 성공적으로 삭제되었습니다."),

    // 사용자 관련 실패 코드
    USER_CREATE_FAILED("E0203", "사용자 생성에 실패했습니다."),
    USER_READ_FAILED("E0204", "사용자 정보 조회에 실패했습니다."),
    USER_UPDATE_FAILED("E0205", "사용자 정보 업데이트에 실패했습니다."),
    USER_DELETE_FAILED("E0206", "사용자 삭제에 실패했습니다.");

    private final String code;
    private final String defaultMessage;

    // 코드 값으로 Enum을 찾는 메서드
    public static ApiResponseCode findByCode(String code) {
        for (ApiResponseCode responseCode : values()) {
            if (responseCode.getCode().equals(code)) {
                return responseCode;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 응답 코드입니다: " + code);
    }

    // 성공 여부 확인 메서드
    public boolean isSuccess() {
        return this.code.startsWith("S");
    }
}
