package com.ptpt.authservice.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserUpdateRequestBody {

    @Schema(description = "사용자 비밀번호(암호화됨)", example = "password123")
    private String password;

    @Schema(description = "사용자 닉네임", example = "example")
    private String nickname;

    @Schema(description = "프로필 이미지 경로", example = "/images/profiles/1/profile.jpg")
    private String profileImage;

    private String interestedSports; // 관심 스포츠 (JSON 문자열)

    @Schema(
            description = "자기소개 (최대 150자)",
            example = "안녕하세요! 운동을 좋아하는 사람입니다.",
            maxLength = 150
    )
    private String bio;

    private String phoneNumber;
}
