package com.ptpt.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.request.EncryptedUserRequestBody;
import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.controller.response.UserResponseDTO;
import com.ptpt.authservice.controller.response.CustomApiResponse;
import com.ptpt.authservice.domain.User;
import com.ptpt.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
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

@Slf4j
@RestController
@Tag(name = "사용자 API", description = "사용자 등록, 정보 조회 및 수정 API")
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "사용자 등록 API",
            description = "새로운 사용자를 등록합니다.",
            tags = {"사용자 API"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용자 등록 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            )
    })
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

            CustomApiResponse<UserResponseDTO> response = CustomApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_CREATE_SUCCESS")
                    .message("사용자가 성공적으로 생성되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CustomApiResponse<UserResponseDTO> errorResponse = CustomApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_CREATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
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
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
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

            // 성공 응답 생성
            CustomApiResponse<UserResponseDTO> response = CustomApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_UPDATE_SUCCESS")
                    .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            CustomApiResponse<UserResponseDTO> errorResponse = CustomApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_UPDATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
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
                            schema = @Schema(implementation = CustomApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomApiResponse.class)
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

            // 성공 응답 생성
            CustomApiResponse<UserResponseDTO> response = CustomApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_INFO_SUCCESS")
                    .message("사용자 정보가 성공적으로 조회되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            CustomApiResponse<UserResponseDTO> errorResponse = CustomApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_INFO_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
