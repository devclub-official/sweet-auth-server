package com.ptpt.authservice.repository.user;

import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.entity.user.UserEntity;
import com.ptpt.authservice.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRepository {

    private final UserJpaRepository userJpaRepository;

    /**
     * 이메일로 사용자 조회
     */
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public Optional<User> findByEmail(String email) {
        log.debug("이메일로 사용자 조회: {}", email);


        log.debug(
                "사용자는 {}입니다.", email
        );
        return userJpaRepository.findUserEntityByEmail(email)
                .map(UserEntity::toDomain);
    }

    /**
     * ID로 사용자 조회
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<User> findById(Long id) {
        log.debug("ID로 사용자 조회: {}", id);
        return userJpaRepository.findById(id)
                .map(UserEntity::toDomain);
    }

    /**
     * 소셜 정보로 사용자 조회
     */
    public Optional<User> findBySocialIdAndSocialType(String socialId, User.SocialType socialType) {
        log.debug("소셜 정보로 사용자 조회 - socialId: {}, type: {}", socialId, socialType);
        return userJpaRepository.findBySocialIdAndSocialType(socialId, socialType)
                .map(UserEntity::toDomain);
    }

    /**
     * 사용자 저장 (생성 또는 업데이트)
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User save(User user) {
        UserEntity entity = (user.getId() != null)
                ? updateExistingUser(user)
                : createNewUser(user);

        UserEntity savedEntity = userJpaRepository.save(entity);
        log.info("사용자 저장 완료 - id: {}, email: {}", savedEntity.getId(), savedEntity.getEmail());

        return savedEntity.toDomain();
    }

    /**
     * 이메일 존재 여부 확인
     */
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    /**
     * 닉네임 존재 여부 확인
     */
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    /**
     * 소셜 ID와 타입으로 존재 여부 확인
     */
    public boolean existsBySocialIdAndSocialType(String socialId, User.SocialType socialType) {
        return userJpaRepository.existsBySocialIdAndSocialType(socialId, socialType);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteById(Long id) {
        if (!userJpaRepository.existsById(id)) {
            throw new UserNotFoundException("삭제할 사용자를 찾을 수 없습니다. ID: " + id);
        }
        userJpaRepository.deleteById(id);
        log.info("사용자 삭제 완료 - id: {}", id);
    }

    /**
     * 닉네임으로 사용자 조회
     */
    public Optional<User> findByNickname(String nickname) {
        return userJpaRepository.findByNickname(nickname)
                .map(UserEntity::toDomain);
    }

    /**
     * 활성화된 사용자 수 조회
     */
    public long countActiveUsers() {
        return userJpaRepository.countByEnabledTrue();
    }

    // ===== Private Helper Methods =====

    private UserEntity updateExistingUser(User user) {
        UserEntity entity = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("업데이트할 사용자를 찾을 수 없습니다. ID: " + user.getId()));
        entity.updateFromDomain(user);
        return entity;
    }

    private UserEntity createNewUser(User user) {
        return UserEntity.fromDomain(user);
    }
}