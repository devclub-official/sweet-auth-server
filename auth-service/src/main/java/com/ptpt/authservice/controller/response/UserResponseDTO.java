package com.ptpt.authservice.controller.response;

import lombok.Builder;
import lombok.Getter;

// 응답용 DTO 클래스 생성
@Getter
@Builder
public class UserResponseDTO {
    private Long id;
    private String email;
    private String username;
    private String profileImage;
}
