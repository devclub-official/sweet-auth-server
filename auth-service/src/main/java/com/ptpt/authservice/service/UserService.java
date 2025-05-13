package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.domain.User;
import com.ptpt.authservice.repository.auth.AuthRepository;
import com.ptpt.authservice.repository.user.UserJpaRepository;
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
        if (userJpaRepository.findUserEntityByEmail(email).isPresent()) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        return authRepository.createNewUser(
                User.builder()
                        .email(email)
                        .password(password)
                        .username(username)
                        .build()
        );
    }

    @Transactional
    // 사용자 정보 업데이트 메서드 추가
    public User updateUserInfo(String email, UserUpdateRequestBody updateRequestBody) {
        // 이메일로 사용자 존재 여부 확인
        authRepository.getUserByUserId(email);

        // 사용자 정보 업데이트
        return authRepository.updateUser(email, updateRequestBody);
    }

    /**
     * 이메일로 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return authRepository.getUserByUserId(email);
    }
}
