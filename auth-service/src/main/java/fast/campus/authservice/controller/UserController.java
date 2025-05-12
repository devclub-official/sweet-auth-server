package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.EncryptedUserRequestBody;
import fast.campus.authservice.controller.request.UserUpdateRequestBody;
import fast.campus.authservice.controller.response.UserUpdateResponse;
import fast.campus.authservice.domain.User;
import fast.campus.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/v1/users")
    public User createNewUser(@RequestBody EncryptedUserRequestBody requestBody) {
        return userService.createNewUser(requestBody.getEmail(), requestBody.getPassword(), requestBody.getUsername());
    }

    @PatchMapping("/api/v1/users")
    public ResponseEntity<UserUpdateResponse<User>> updateUser(
            @AuthenticationPrincipal User userDetails,
            @RequestBody UserUpdateRequestBody userUpdateRequestBody) {

        try {
            User user = User.builder()
                    .username(userUpdateRequestBody.getUsername())
                    .profileImage(userUpdateRequestBody.getProfileImage())
                    .build();

            // 사용자 정보 업데이트
            User updatedUser = userService.updateUserInfo(userDetails.getEmail(), user);

            // 성공 응답 생성
            UserUpdateResponse<User> response = UserUpdateResponse.<User>builder()
                    .success(true)
                    .code("USER_UPDATE_SUCCESS")
                    .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                    .data(updatedUser)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 실패 응답 생성
            UserUpdateResponse<User> errorResponse = UserUpdateResponse.<User>builder()
                    .success(false)
                    .code("USER_UPDATE_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
