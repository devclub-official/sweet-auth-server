package fast.campus.movieservice.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 로그인 후 ID, Password 검증이 끝난 후 OTP 검증이 끝났을 때
// 이후에 얻는 Token 을 가지고 호출되는 endpoint
@RestController
public class TestController {
    @GetMapping("/api/v1/test")
    public String test() {
        return "TEST SUCCESS";
    }
}
