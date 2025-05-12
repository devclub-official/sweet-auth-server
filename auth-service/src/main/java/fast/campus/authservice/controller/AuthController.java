package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.LoginRequest;
import fast.campus.authservice.controller.request.SimpleUserRequestBody;
import fast.campus.authservice.controller.response.ApiResponse;
import fast.campus.authservice.domain.User;
import fast.campus.authservice.dto.response.TokenResponseDto;
import fast.campus.authservice.service.AuthService;
import fast.campus.authservice.service.UserService;
import fast.campus.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpSec;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpSec;

//    @PostMapping("/users")
//    public String auth(@RequestBody SimpleUserRequestBody requestBody) {
//        return userService.auth(requestBody.getEmail(), requestBody.getPassword());
//    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@RequestBody LoginRequest loginRequest) {
        try {
            TokenResponseDto tokenResponseDto = authService.authenticateUser(loginRequest);

            ApiResponse<TokenResponseDto> response = ApiResponse.<TokenResponseDto>builder()
                    .success(true)
                    .code("LOGIN_SUCCESS")
                    .message("로그인이 성공적으로 완료되었습니다.")
                    .data(tokenResponseDto)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<TokenResponseDto> errorResponse = ApiResponse.<TokenResponseDto>builder()
                    .success(false)
                    .code("LOGIN_FAILED")
                    .message(e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
