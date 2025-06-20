package com.ptpt.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.request.EncryptedUserRequestBody;
import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.controller.response.UserProfileResponse;
import com.ptpt.authservice.controller.response.UserResponse;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exception.AuthServiceException;
import com.ptpt.authservice.service.UserService;
import com.ptpt.authservice.swagger.SwaggerErrorResponseDTO;
import com.ptpt.authservice.swagger.UserControllerDocs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//https://infinitecode.tistory.com/65
//인터페이스로 스웨거 관련 어노테이션 가독성 향상 방법
@Slf4j
@RestController
@Tag(name = "사용자 API", description = "사용자 등록, 정보 조회 및 수정 API")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<CustomApiResponse<UserResponse>> createNewUser(@RequestBody EncryptedUserRequestBody requestBody) {
        User newUser = userService.createNormalUser(requestBody.getEmail(), requestBody.getPassword(), requestBody.getUsername());

        UserResponse responseData = UserResponse.builder()
                .id(newUser.getId())
                .email(newUser.getEmail())
                .username(newUser.getNickname())
                .profileImage(newUser.getProfileImage())
                .build();

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_CREATE_SUCCESS, responseData));
    }

    @Operation(
            summary = "사용자 정보 수정 API",
            description = "현재 로그인한 사용자의 정보를 수정합니다. userInfo는 JSON 형식의 문자열로 제공해야 하며, profileImage는 이미지 파일로 제공합니다.",
            security = @SecurityRequirement(name = "BearerAuth"),
            tags = {"사용자 API"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용자 정보 수정 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    @PatchMapping(value = "/users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CustomApiResponse<UserResponse>> updateUser(
        @AuthenticationPrincipal User userDetails,
        @Parameter(
                description = "사용자 정보를 담은 JSON 문자열 (예: {\"username\":\"홍길동\"})",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
        @RequestPart(value = "userInfo", required = false) String userInfoJson,

        @Parameter(
                description = "사용자 프로필 이미지 파일",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        UserUpdateRequestBody userUpdateRequestBody = null;
        if (userInfoJson != null && !userInfoJson.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                userUpdateRequestBody = objectMapper.readValue(userInfoJson, UserUpdateRequestBody.class);
            } catch (Exception e) {
                throw new AuthServiceException(ApiResponseCode.USER_UPDATE_FAILED, "잘못된 JSON 형식입니다.");
            }
        } else {
            userUpdateRequestBody = new UserUpdateRequestBody();
        }

        log.info("프로필 이미지 요청: {}", profileImage != null ? profileImage.getOriginalFilename() : "없음");
        log.info("소개글 {}", userUpdateRequestBody.getBio());

        // 사용자 정보 업데이트
        User updatedUser = userService.updateUserInfo(userDetails.getEmail(), userUpdateRequestBody, profileImage);

        UserResponse responseData = UserResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .username(updatedUser.getNickname())
                .profileImage(updatedUser.getProfileImage())
                .bio(updatedUser.getBio())
                .build();

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_UPDATE_SUCCESS, responseData));
    }

    @Operation(
            summary = "사용자 정보 조회 API",
            description = "현재 로그인한 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "BearerAuth"),
            tags = {"사용자 API"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용자 정보 조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping(value = "/users")
    public ResponseEntity<CustomApiResponse<UserResponse>> getUserInfo(@AuthenticationPrincipal User userDetails) {
        // 인증된 사용자의 정보를 가져옵니다
        User user = userService.getUserByEmail(userDetails.getEmail());

        log.info("관심 스포츠 JSON: {}", user.getInterestedSports());
        log.info("관심 스포츠 List: {}", user.getInterestedSportsList());

        UserResponse responseData = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getNickname())
                .profileImage(user.getProfileImage())
                .interestedSports(user.getInterestedSportsList())
                .bio(user.getBio())
                .build();

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_READ_SUCCESS, responseData));
    }

    /**
     * 사용자 ID로 프로필 조회 API
     */
    @Operation(
            summary = "사용자 프로필 조회 API",
            description = "사용자 ID로 특정 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "BearerAuth"),
            tags = {"사용자 API"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SwaggerErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/profiles/{userId}")
    public ResponseEntity<CustomApiResponse<UserProfileResponse>> getUserById(
            @AuthenticationPrincipal User userDetails,
            @PathVariable Long userId) {

        UserProfileResponse userProfile = userService.getUserProfileById(userId);

        return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_READ_SUCCESS, userProfile));
    }
}
