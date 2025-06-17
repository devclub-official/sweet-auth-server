package com.ptpt.authservice.controller;
//
//import com.ptpt.authservice.controller.response.CustomApiResponse;
//import com.ptpt.authservice.dto.kakao.KakaoUserInfoResponse;
//import com.ptpt.authservice.controller.response.SocialLoginResponse;
//import com.ptpt.authservice.enums.ApiResponseCode;
//import com.ptpt.authservice.service.AuthService;
//import com.ptpt.authservice.service.KakaoServiceBefore;
//import com.ptpt.authservice.swagger.SwaggerErrorResponseDTO;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.ExampleObject;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//

import com.ptpt.authservice.controller.request.AccessTokenRequest;
import com.ptpt.authservice.controller.request.CompleteSignupRequest;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.controller.response.TokenResponse;
import com.ptpt.authservice.controller.response.SocialLoginResponse;
import com.ptpt.authservice.dto.SocialUserInfo;
import com.ptpt.authservice.dto.TempUserInfo;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.enums.SocialProvider;
import com.ptpt.authservice.enums.SocialSignupRequiredField;
import com.ptpt.authservice.service.AuthService;
import com.ptpt.authservice.service.SocialService;
import com.ptpt.authservice.service.TokenService;
import com.ptpt.authservice.service.UserService;
import com.ptpt.authservice.swagger.SwaggerAuthResponseDTO;
import com.ptpt.authservice.swagger.SwaggerErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

//// 카카오 로그인 구현: https://ddonghyeo.tistory.com/16
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/social/callback")
//@Tag(name = "소셜 로그인 API",
//        description = "클라이언트에서 받은 액세스 토큰으로 소셜 로그인을 처리하는 API입니다. "
//)
//public class SocialController {
//
//    private final KakaoServiceBefore kakaoService;
//    private final AuthService authService;
//
//    @Operation(
//            summary = "카카오 로그인 콜백 API",
//            description = """
//                    카카오 OAuth 인증 완료 후 전달받은 인증 코드를 처리합니다.
//
//                    **처리 과정:**
//                    1. 인증 코드를 카카오 액세스 토큰으로 교환
//                    2. 액세스 토큰으로 카카오 사용자 정보 조회
//                    3. 기존 사용자 확인 후 로그인 또는 회원가입 프로세스 진행
//
//                    **응답 유형:**
//                    - **로그인 성공**: 기존 소셜 사용자인 경우 즉시 JWT 토큰 발급
//                    - **회원가입 필요**: 신규 사용자인 경우 임시 토큰과 추가 정보 입력 요청
//
//                    **주의사항:**
//                    - 이 API는 일반적으로 프론트엔드에서 직접 호출하지 않습니다
//                    - 카카오 로그인 후 자동 리다이렉트되는 URL입니다
//                    - 인증 코드는 일회성이며 짧은 시간 내에 사용해야 합니다
//                    """
//            // tags = {"소셜 로그인 콜백 API"}
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "카카오 로그인 처리 성공",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = CustomApiResponse.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "기존 사용자 로그인 성공",
//                                            summary = "이미 가입된 소셜 사용자의 로그인 성공 응답",
//                                            value = """
//                                                    {
//                                                      "success": true,
//                                                      "code": "S0111",
//                                                      "message": "소셜 로그인이 성공적으로 완료되었습니다.",
//                                                      "data": {
//                                                        "status": "LOGIN_SUCCESS",
//                                                        "tokens": {
//                                                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//                                                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//                                                          "tokenType": "Bearer"
//                                                        }
//                                                      }
//                                                    }
//                                                    """
//                                    ),
//                                    @ExampleObject(
//                                            name = "신규 사용자 회원가입 필요",
//                                            summary = "추가 정보 입력이 필요한 신규 사용자 응답",
//                                            value = """
//                                                    {
//                                                      "success": true,
//                                                      "code": "S0112",
//                                                      "message": "소셜 회원가입을 위한 추가 정보 입력이 필요합니다.",
//                                                      "data": {
//                                                        "status": "SIGNUP_REQUIRED",
//                                                        "tempToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//                                                        "tempUserInfo": {
//                                                          "email": "user@kakao.com",
//                                                          "socialId": "123456789",
//                                                          "socialType": "KAKAO",
//                                                          "nickname": "카카오사용자",
//                                                          "profileImageUrl": "https://k.kakaocdn.net/profile.jpg"
//                                                        },
//                                                        "requiredFields": ["phoneNumber", "agreeTerms"]
//                                                      }
//                                                    }
//                                                    """
//                                    )
//                            }
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "400",
//                    description = "카카오 로그인 처리 실패",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "유효하지 않은 인증 코드",
//                                            summary = "만료되거나 잘못된 인증 코드로 인한 실패",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "code": "E0117",
//                                                      "message": "소셜 플랫폼과의 통신 중 오류가 발생했습니다.",
//                                                      "data": null
//                                                    }
//                                                    """
//                                    ),
//                                    @ExampleObject(
//                                            name = "이메일 중복 오류",
//                                            summary = "다른 방식으로 이미 가입된 이메일인 경우",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "code": "E0116",
//                                                      "message": "해당 이메일로 이미 가입된 계정이 있습니다.",
//                                                      "data": null
//                                                    }
//                                                    """
//                                    ),
//                                    @ExampleObject(
//                                            name = "일반적인 로그인 실패",
//                                            summary = "기타 소셜 로그인 처리 오류",
//                                            value = """
//                                                    {
//                                                      "success": false,
//                                                      "code": "E0111",
//                                                      "message": "소셜 로그인에 실패했습니다.",
//                                                      "data": null
//                                                    }
//                                                    """
//                                    )
//                            }
//                    )
//            ),
//            @ApiResponse(
//                    responseCode = "500",
//                    description = "서버 내부 오류",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class),
//                            examples = @ExampleObject(
//                                    name = "서버 오류",
//                                    value = """
//                                            {
//                                              "success": false,
//                                              "code": "E0300",
//                                              "message": "서버 내부 오류가 발생했습니다.",
//                                              "data": null
//                                            }
//                                            """
//                            )
//                    )
//            )
//    })
//    @GetMapping("/kakao")
//    public ResponseEntity<?> kakao(
//            @Parameter(
//                    name = "code",
//                    description = """
//                            카카오 OAuth 인증 완료 후 전달받는 인증 코드입니다.
//
//                            **특징:**
//                            - 일회성 코드로 한 번만 사용 가능
//                            - 약 10분의 짧은 유효시간을 가짐
//                            - 카카오 로그인 성공 후 자동으로 리다이렉트 URL에 포함되어 전달
//
//                            **주의사항:**
//                            - 코드 값을 수동으로 입력하지 마세요
//                            - 실제 환경에서는 카카오에서 자동으로 제공됩니다
//                            """,
//                    required = true,
//                    example = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890",
//                    schema = @Schema(type = "string", minLength = 20, maxLength = 100)
//            )
//            @RequestParam("code") String code) {
//        try {
//            log.info("카카오 로그인 콜백 - code={}", code);
//
//            // 1. 카카오에서 액세스 토큰 획득
//            String accessToken = kakaoService.getAccessTokenFromKakao(code);
//            log.info("카카오 액세스 토큰 획득 성공");
//
//            // 2. 액세스 토큰으로 사용자 정보 획득
//            KakaoUserInfoResponse userInfo = kakaoService.getUserInfo(accessToken);
//            log.info("카카오 사용자 정보 획득 - email={}", userInfo.getKakaoAccount().getEmail());
//
//            // 3. 로그인/회원가입 처리
//            SocialLoginResponse response = authService.handleKakaoLogin(userInfo);
//
//            ApiResponseCode responseCode = "LOGIN_SUCCESS".equals(response.getStatus())
//                    ? ApiResponseCode.AUTH_SOCIAL_LOGIN_SUCCESS
//                    : ApiResponseCode.AUTH_SOCIAL_SIGNUP_REQUIRED;
//
//            return ResponseEntity.ok(CustomApiResponse.of(responseCode, response));
//
//        } catch (IllegalArgumentException e) {
//            // 비즈니스 로직 오류 (이메일 중복 등)
//            log.warn("카카오 로그인 비즈니스 로직 오류: {}", e.getMessage());
//            ApiResponseCode errorCode = e.getMessage().contains("이미 가입된")
//                    ? ApiResponseCode.AUTH_SOCIAL_EMAIL_ALREADY_EXISTS
//                    : ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED;
//
//            return ResponseEntity.badRequest().body(
//                    CustomApiResponse.of(errorCode, e.getMessage(), null));
//
//        } catch (RuntimeException e) {
//            // 외부 API 통신 오류
//            log.error("카카오 API 통신 오류: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(
//                    CustomApiResponse.of(ApiResponseCode.AUTH_SOCIAL_PLATFORM_ERROR, e.getMessage(), null));
//
//        } catch (Exception e) {
//            // 기타 예상치 못한 오류
//            log.error("카카오 로그인 처리 중 예상치 못한 오류 발생", e);
//            return ResponseEntity.badRequest().body(
//                    CustomApiResponse.of(ApiResponseCode.AUTH_SOCIAL_LOGIN_FAILED, "소셜 로그인 처리 중 오류가 발생했습니다.", null));
//        }
//    }
//}


@Slf4j
@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final Map<SocialProvider, SocialService> socialServices;

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<CustomApiResponse<?>> socialLogin(@RequestBody AccessTokenRequest request) {
        log.info("소셜 로그인 요청 - provider: {}", request.getProvider());

        // 소셜 제공자 enum으로 변환
        SocialProvider provider = SocialProvider.fromString(request.getProvider());

        // 해당 제공자에 맞는 서비스 선택
        SocialService service = getSocialService(provider);

        // 1. 소셜 플랫폼에서 사용자 정보 조회
        SocialUserInfo socialUserInfo = service.getUserInfo(request.getAccessToken());
        log.info("소셜 사용자 정보 조회 완료 - email: {}", socialUserInfo.getEmail());

        // 2. 기존 사용자 확인
        Optional<User> existingUser = userService.findByEmail(socialUserInfo.getEmail());

        if (existingUser.isPresent()) {
            // 3-1. 기존 사용자인 경우 - 토큰 발급
            log.info("기존 사용자 로그인 - userId: {}", existingUser.get().getId());

            TokenResponse tokens = tokenService.generateTokens(existingUser.get());

            return ResponseEntity.ok(
                    CustomApiResponse.of(ApiResponseCode.AUTH_SOCIAL_LOGIN_SUCCESS, "소셜 로그인이 성공적으로 완료되었습니다.", tokens)
            );

        } else {
            // 3-2. 신규 사용자인 경우 - 추가 정보 입력 요청
            log.info("신규 사용자 - 추가 정보 입력 필요");

            // 신규 사용자 - 임시 토큰 발급
            TempUserInfo tempUserInfo = TempUserInfo.builder()
                    .email(socialUserInfo.getEmail())
                    .socialId(socialUserInfo.getSocialId())
                    .socialType(convertToSocialType(provider))
                    .nickname(socialUserInfo.getNickname())
                    .profileImageUrl(socialUserInfo.getProfileImageUrl())
                    .build();

            // 임시 토큰 생성
            String tempToken = tokenService.generateTempToken(tempUserInfo);

            // 추가로 입력받을 필드들 정의
            List<String> requiredFields = SocialSignupRequiredField.getRequiredFields();
            List<String> optionalFields = SocialSignupRequiredField.getOptionalFields();

            // 필드 정보 리스트 생성
            List<SocialSignupRequiredField.FieldInfo> fieldInfoList = SocialSignupRequiredField.getFieldInfoList();

            // 관심 스포츠 옵션 제공
            List<String> sportsOptions = getSportsOptions();

            // 거주지 옵션 제공
            List<String> locationOptions = getLocationOptions();

            SocialLoginResponse response = SocialLoginResponse.builder()
                    .status("SIGNUP_REQUIRED")
                    .tempToken(tempToken)
                    .tempUserInfo(tempUserInfo)
                    .requiredFields(requiredFields)
                    .optionalFields(optionalFields)
                    .fieldInfoList(fieldInfoList)
                    .sportsOptions(sportsOptions)
                    .locationOptions(locationOptions)
                    .build();

            return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.AUTH_SOCIAL_SIGNUP_REQUIRED,
                    "소셜 회원가입을 위한 추가 정보 입력이 필요합니다.", response));
        }
    }

    /**
     * 관심 스포츠 옵션 목록 반환
     */
    private List<String> getSportsOptions() {
        return List.of(
                "헬스", "러닝", "요가", "클라이밍", "수영", "사이클링"
        );
    }

    /**
     * 거주지 옵션 목록 반환
     */
    private List<String> getLocationOptions() {
        return List.of(
                "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
                "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도",
                "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
                "경상남도", "제주특별자치도"
        );
    }

    @Operation(
            summary = "소셜 회원가입 완료 API",
            description = "임시 토큰을 사용하여 소셜 회원가입을 완료합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerAuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원가입 완료 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })

    @PostMapping("/signup/complete")
    public ResponseEntity<CustomApiResponse<TokenResponse>> completeSocialSignup(
            @RequestHeader("Authorization") String tempToken,
            @RequestBody CompleteSignupRequest request) {

        // "Bearer " 제거
        String token = tempToken.replace("Bearer ", "");
        log.info("소셜 회원가입 완료 요청 - nickname={}", request.getNickname());

        TokenResponse tokens = authService.completeSocialSignup(token, request);

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_CREATE_SUCCESS, tokens));
    }

    /**
     * 소셜 제공자에 맞는 서비스 반환
     */
    private SocialService getSocialService(SocialProvider provider) {
        SocialService service = socialServices.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
        }
        return service;
    }

    /**
     * SocialProvider를 User.SocialType으로 변환
     */
    private User.SocialType convertToSocialType(SocialProvider provider) {
        return switch (provider) {
            case KAKAO -> User.SocialType.KAKAO;
            case NAVER -> User.SocialType.NAVER;
            case APPLE -> User.SocialType.APPLE;
            case NONE -> null;
        };
    }

}
