package com.ptpt.authservice.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.enums.ApiResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponseCode responseCode = determineResponseCode(authException, request);
        CustomApiResponse<Void> apiResponse = CustomApiResponse.of(responseCode, null);

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    private ApiResponseCode determineResponseCode(AuthenticationException ex, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponseCode.AUTH_TOKEN_MISSING;
        }

        // 예외 타입에 따른 구분
        String exceptionName = ex.getClass().getSimpleName();
        return switch (exceptionName) {
            case "BadCredentialsException" -> ApiResponseCode.AUTH_LOGIN_FAILED;
            case "InsufficientAuthenticationException" -> ApiResponseCode.AUTH_UNAUTHORIZED;
            case "DisabledException", "AccountExpiredException",
                 "CredentialsExpiredException", "LockedException" -> ApiResponseCode.AUTH_LOGIN_FAILED;
            default -> ApiResponseCode.AUTH_UNAUTHORIZED;
        };
    }
}
