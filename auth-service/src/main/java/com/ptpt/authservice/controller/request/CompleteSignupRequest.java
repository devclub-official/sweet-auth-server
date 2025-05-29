package com.ptpt.authservice.controller.request;

import com.ptpt.authservice.enums.SocialSignupRequiredField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteSignupRequest {
    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname; // 카카오에서 받은 닉네임을 수정할 수 있음

    private String phoneNumber;

    @NotNull(message = "약관 동의는 필수입니다")
    private boolean agreeTerms;

    private String bio;

    /**
     * 필수 필드 검증
     */
    public void validateRequiredFields() {
        SocialSignupRequiredField.validateRequiredFields(this);

        // 추가 비즈니스 로직 검증
        if (!agreeTerms) {
            throw new IllegalArgumentException("서비스 이용약관에 동의해야 합니다.");
        }
    }
}
