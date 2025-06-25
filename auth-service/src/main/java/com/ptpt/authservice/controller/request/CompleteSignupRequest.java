package com.ptpt.authservice.controller.request;

import com.ptpt.authservice.enums.SocialSignupRequiredField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CompleteSignupRequest {

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다")
    private String nickname; // 카카오에서 받은 닉네임을 수정할 수 있음

    private String phoneNumber;

//    @NotNull(message = "생년월일은 필수입니다")
    @Past(message = "생년월일은 과거 날짜여야 합니다")
    private LocalDate birthDate; // 생년월일

    @NotBlank(message = "거주지는 필수입니다")
    private String location; // 거주지 (시/도 단위)

    @NotNull(message = "관심 스포츠는 최소 1개 이상 선택해야 합니다")
    @Size(min = 1, message = "관심 스포츠는 최소 1개 이상 선택해야 합니다")
    private List<String> interestedSports; // 관심 스포츠 목록

    private String profileImageUrl; // 프로필 이미지 URL (선택사항)

//    @NotNull(message = "약관 동의는 필수입니다")
    private Boolean agreeTerms;

    private String bio;

    /**
     * 필수 필드 검증
     */
    public void validateRequiredFields() {
        SocialSignupRequiredField.validateRequiredFields(this);

        // 추가 비즈니스 로직 검증
//        if (agreeTerms == null || !agreeTerms) {
//            throw new IllegalArgumentException("서비스 이용약관에 동의해야 합니다.");
//        }

        // 생년월일 검증 (만 14세 이상)
        if (birthDate != null && birthDate.isAfter(LocalDate.now().minusYears(14))) {
            throw new IllegalArgumentException("만 14세 이상만 가입할 수 있습니다.");
        }

        // 관심 스포츠 개수 제한 (최대 5개)
        if (interestedSports != null && interestedSports.size() > 5) {
            throw new IllegalArgumentException("관심 스포츠는 최대 5개까지 선택할 수 있습니다.");
        }

        // 거주지 유효성 검증 (한국 시/도 목록)
//        if (location != null && !isValidLocation(location)) {
//            throw new IllegalArgumentException("올바른 거주지를 선택해주세요.");
//        }
    }

    /**
     * 유효한 거주지인지 확인
     */
    private boolean isValidLocation(String location) {
        List<String> validLocations = List.of(
                "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
                "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도",
                "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
                "경상남도", "제주특별자치도"
        );
        return validLocations.contains(location);
    }
}