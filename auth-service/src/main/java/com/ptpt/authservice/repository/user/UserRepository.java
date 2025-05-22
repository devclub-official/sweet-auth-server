package com.ptpt.authservice.repository.user;

import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserJpaRepository userJpaRepository;

    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findUserEntityByEmail(email)
                .map(UserEntity::toDomain);
    }

    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    public Optional<User> findBySocialInfo(String socialId, User.SocialType socialType) {
        return userJpaRepository.findBySocialInfo(socialId, socialType)
                .map(UserEntity::toDomain);
    }

    public User save(User user) {
        UserEntity entity;
        if (user.getId() != null) {
            // 기존 사용자 업데이트
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            entity.updateFromDomain(user);
        } else {
            // 새 사용자 생성
            entity = UserEntity.fromDomain(user);
        }

        UserEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}
