package fast.campus.authservice.service;

import fast.campus.authservice.domain.User;
import fast.campus.authservice.entity.user.UserEntity;
import fast.campus.authservice.repository.auth.AuthRepository;
import fast.campus.authservice.repository.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final UserJpaRepository userJpaRepository;
    private final EncryptService encryptService;

    public User createNewUser(String email, String password, String username) {
        return authRepository.createNewUser(
                User.builder()
                        .email(email)
                        .password(password)
                        .username(username)
                        .build()
        );
    }

    public String auth(String userId, String password) {
        User user = authRepository.getUserByUserId(userId);

        if (encryptService.matches(password, user.getPassword())) {
            return "인증 성공";
        }

        throw new RuntimeException("비밀번호 맞지 않음!");
    }

    @Transactional
    // 사용자 정보 업데이트 메서드 추가
    public User updateUserInfo(String email, User user) {
        // 이메일로 사용자 존재 여부 확인
        authRepository.getUserByUserId(email);

        // 사용자 정보 업데이트
        return authRepository.updateUser(email, user);
    }
}
