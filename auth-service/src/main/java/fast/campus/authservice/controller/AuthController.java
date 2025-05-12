package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.LoginRequest;
import fast.campus.authservice.controller.request.SimpleUserRequestBody;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserService userService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpSec;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpSec;

    @PostMapping("/users/auth")
    public String auth(@RequestBody SimpleUserRequestBody requestBody) {
        return userService.auth(requestBody.getEmail(), requestBody.getPassword());
    }

    @PostMapping("/users/auth/token")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequest loginRequest) {
        TokenResponseDto tokenResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }
}
