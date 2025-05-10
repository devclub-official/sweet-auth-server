package fast.campus.movieservice.delegator;

// Auth service (인증 서버)를 호출하는 코드들이 존재

import fast.campus.movieservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AuthenticationDelegator {
    private final RestTemplate restTemplate;

    @Value("${base-url.auth-server}")
    private String authServiceBaseUrl;

//    1번째 메서드: id, password 를 검증하는 api
    public void restAuth(String userId, String password) {
        String url = authServiceBaseUrl + "/users/auth";

        User user = User.builder()
                .userId(userId)
                .password(password)
                .build();

        restTemplate.postForEntity(url, new HttpEntity<>(user), Void.class);
    }

//    2번째 메서드: otp 를 검증하는 api
    public boolean restOtp(String userId, String otp) {
        String url = authServiceBaseUrl + "/otp/check";

        User user = User.builder()
                .userId(userId)
                .otp(otp)
                .build();

        ResponseEntity<Boolean> response = restTemplate.postForEntity(url, new HttpEntity<>(user), Boolean.class);
        return Boolean.TRUE.equals(response.getBody());
    }
}
