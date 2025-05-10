package fast.campus.authservice.service;

import fast.campus.authservice.domain.User;
import fast.campus.authservice.repository.auth.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final EncryptService encryptService;

    public User createNewUser(String userId, String password, String username) {
        return authRepository.createNewUser(new User(userId, password, username));
    }

    public String auth(String userId, String password) {
        User user = authRepository.getUserByUserId(userId);

        if (encryptService.matches(password, user.getPassword())) {
            return "인증 성공";
        }

        throw new RuntimeException("비밀번호 맞지 않음!");
    }
}
