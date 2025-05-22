package com.ptpt.authservice.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteSignupRequest {
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname; // 카카오에서 받은 닉네임을 수정할 수 있음
    private String phoneNumber;
    private boolean agreeTerms;
    private String bio;
}
