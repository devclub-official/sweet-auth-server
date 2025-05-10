package fast.campus.authservice.controller;

import fast.campus.authservice.controller.request.SimpleUserRequestBody;
import fast.campus.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/api/v1/users/auth")
    public String auth(@RequestBody SimpleUserRequestBody requestBody) {
        return userService.auth(requestBody.getEmail(), requestBody.getPassword());
    }
}
