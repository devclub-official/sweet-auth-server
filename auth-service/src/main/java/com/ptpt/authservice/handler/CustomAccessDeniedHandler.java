package com.ptpt.authservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        CustomApiResponse<Void> apiResponse = CustomApiResponse.<Void>builder()
                .success(false)
                .code("FORBIDDEN")
                .message("이 리소스에 대한 접근 권한이 없습니다.")
                .data(null)
                .build();

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}