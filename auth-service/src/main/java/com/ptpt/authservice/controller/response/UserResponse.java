package com.ptpt.authservice.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 응답용 DTO 클래스 생성
@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String profileImage;
    private List<String> interestedSports; // 관심 스포츠 (JSON 문자열)
    private String bio;
}
