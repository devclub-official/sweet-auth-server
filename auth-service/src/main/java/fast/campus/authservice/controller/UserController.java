package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.EncryptedUserRequestBody;
import fast.campus.authservice.controller.request.UserUpdateRequestBody;
import fast.campus.authservice.controller.response.UserResponseDTO;
import fast.campus.authservice.controller.response.ApiResponse;
import fast.campus.authservice.domain.User;
import fast.campus.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createNewUser(@RequestBody EncryptedUserRequestBody requestBody) {
        try {
            User newUser = userService.createNewUser(requestBody.getEmail(), requestBody.getPassword(), requestBody.getUsername());

            UserResponseDTO responseData = UserResponseDTO.builder()
                    .id(newUser.getId())
                    .email(newUser.getEmail())
                    .username(newUser.getNickname())
                    .profileImage(newUser.getProfileImage())
                    .build();

            ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_CREATE_SUCCESS")
                    .message("사용자가 성공적으로 생성되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<UserResponseDTO> errorResponse = ApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_CREATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PatchMapping(value = "/users")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @AuthenticationPrincipal User userDetails,
            @RequestBody UserUpdateRequestBody userUpdateRequestBody) {

        try {

            // 사용자 정보 업데이트
            User updatedUser = userService.updateUserInfo(userDetails.getEmail(), userUpdateRequestBody);

            UserResponseDTO responseData = UserResponseDTO.builder()
                    .id(updatedUser.getId())
                    .email(updatedUser.getEmail())
                    .username(updatedUser.getNickname())
                    .profileImage(updatedUser.getProfileImage())
                    .build();

            // 성공 응답 생성
            ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_UPDATE_SUCCESS")
                    .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            ApiResponse<UserResponseDTO> errorResponse = ApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_UPDATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping(value = "/users")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserInfo(@AuthenticationPrincipal User userDetails) {
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
            ApiResponse<UserResponseDTO> response = ApiResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_INFO_SUCCESS")
                    .message("사용자 정보가 성공적으로 조회되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            ApiResponse<UserResponseDTO> errorResponse = ApiResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_INFO_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
