package com.ptpt.authservice.dto.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ptpt.authservice.service.NaverService;
import lombok.Data;

/**
 * 네이버 사용자 정보 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfoResponse {
    private String resultcode;
    private String message;
    private NaverUserInfo response;
}
