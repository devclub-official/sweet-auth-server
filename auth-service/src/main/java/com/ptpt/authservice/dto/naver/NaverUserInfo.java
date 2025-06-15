package com.ptpt.authservice.dto.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 네이버 사용자 정보 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfo {
    private String id;
    private String email;
    private String nickname;
    private String name;

    @JsonProperty("profile_image")
    private String profileImageUrl;

    private String age;
    private String gender;
    private String birthday;
    private String birthyear;
    private String mobile;
}
