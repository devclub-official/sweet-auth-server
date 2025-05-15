package com.ptpt.authservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import jakarta.servlet.ServletException;
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
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        CustomApiResponse<Void> apiResponse = CustomApiResponse.<Void>builder()
                .success(false)
                .code("UNAUTHORIZED")
                .message("인증에 실패했습니다. 유효한 자격 증명이 필요합니다.")
                .data(null)
                .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
