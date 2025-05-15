package com.ptpt.authservice.handler;

import com.ptpt.authservice.controller.response.CustomApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        String code = "AUTHENTICATION_ERROR";
        String message = "인증 오류가 발생했습니다.";

        if (ex instanceof BadCredentialsException) {
            code = "INVALID_CREDENTIALS";
            message = "아이디 또는 비밀번호가 잘못되었습니다.";
        } else if (ex instanceof InsufficientAuthenticationException) {
            code = "INSUFFICIENT_AUTHENTICATION";
            message = "인증 정보가 충분하지 않습니다.";
        }

        CustomApiResponse<Void> response = CustomApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        CustomApiResponse<Void> response = CustomApiResponse.<Void>builder()
                .success(false)
                .code("ACCESS_DENIED")
                .message("접근 권한이 없습니다.")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 기타 예외 처리를 추가할 수 있습니다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse<Void>> handleException(Exception ex) {
        CustomApiResponse<Void> response = CustomApiResponse.<Void>builder()
                .success(false)
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}