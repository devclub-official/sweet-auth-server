package fast.campus.movieservice.domain.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginRequest {
    private final String userId;
    private final String password;
    private final String otp;
}
