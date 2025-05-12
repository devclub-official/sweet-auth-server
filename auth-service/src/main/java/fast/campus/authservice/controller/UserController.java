package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.EncryptedUserRequestBody;
import fast.campus.authservice.controller.request.UserUpdateRequestBody;
import fast.campus.authservice.controller.response.UserResponseDTO;
import fast.campus.authservice.controller.response.UserUpdateResponse;
import fast.campus.authservice.domain.User;
import fast.campus.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public User createNewUser(@RequestBody EncryptedUserRequestBody requestBody) {
        return userService.createNewUser(requestBody.getEmail(), requestBody.getPassword(), requestBody.getUsername());
    }

    @PatchMapping(value = "/users")
    public ResponseEntity<UserUpdateResponse<UserResponseDTO>> updateUser(
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
            UserUpdateResponse<UserResponseDTO> response = UserUpdateResponse.<UserResponseDTO>builder()
                    .success(true)
                    .code("USER_UPDATE_SUCCESS")
                    .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            UserUpdateResponse<UserResponseDTO> errorResponse = UserUpdateResponse.<UserResponseDTO>builder()
                    .success(false)
                    .code("USER_UPDATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
