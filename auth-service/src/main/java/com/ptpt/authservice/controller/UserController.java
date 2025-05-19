package com.ptpt.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.request.EncryptedUserRequestBody;
import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.controller.response.UserResponseDTO;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.domain.User;
import com.ptpt.authservice.enums.ApiResponseCode;
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
    public ResponseEntity<CustomApiResponse<UserResponseDTO>> createNewUser(@RequestBody EncryptedUserRequestBody requestBody) {
        try {
            User newUser = userService.createNewUser(requestBody.getEmail(), requestBody.getPassword(), requestBody.getUsername());

            UserResponseDTO responseData = UserResponseDTO.builder()
                    .id(newUser.getId())
                    .email(newUser.getEmail())
                    .username(newUser.getNickname())
                    .profileImage(newUser.getProfileImage())
                    .build();

            return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_CREATE_SUCCESS, responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    CustomApiResponse.of(ApiResponseCode.USER_CREATE_FAILED, e.getMessage(), null));
        }
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
    public ResponseEntity<CustomApiResponse<UserResponseDTO>> updateUser(
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
        try {
            UserUpdateRequestBody userUpdateRequestBody = null;
            if (userInfoJson != null && !userInfoJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                userUpdateRequestBody = objectMapper.readValue(userInfoJson, UserUpdateRequestBody.class);
            } else {
                userUpdateRequestBody = new UserUpdateRequestBody();
            }

            log.info("프로필 이미지 요청: {}", profileImage != null ? profileImage.getOriginalFilename() : "없음");


            // 사용자 정보 업데이트
            User updatedUser = userService.updateUserInfo(userDetails.getEmail(), userUpdateRequestBody, profileImage);

            UserResponseDTO responseData = UserResponseDTO.builder()
                    .id(updatedUser.getId())
                    .email(updatedUser.getEmail())
                    .username(updatedUser.getNickname())
                    .profileImage(updatedUser.getProfileImage())
                    .build();

            return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_UPDATE_SUCCESS, responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    CustomApiResponse.of(ApiResponseCode.USER_UPDATE_FAILED, e.getMessage(), null));
        }
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
    public ResponseEntity<CustomApiResponse<UserResponseDTO>> getUserInfo(@AuthenticationPrincipal User userDetails) {
        try {
            // 인증된 사용자의 정보를 가져옵니다
            User user = userService.getUserByEmail(userDetails.getEmail());

            UserResponseDTO responseData = UserResponseDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getNickname())
                    .profileImage(user.getProfileImage())
                    .build();

            return ResponseEntity.ok(CustomApiResponse.of(ApiResponseCode.USER_READ_SUCCESS, responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    CustomApiResponse.of(ApiResponseCode.USER_READ_FAILED, e.getMessage(), null));
        }
    }
}
