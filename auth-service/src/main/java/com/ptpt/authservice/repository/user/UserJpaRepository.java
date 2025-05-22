package com.ptpt.authservice.repository.user;

import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByEmail(String userId);

    // 소셜 로그인 관련 쿼리
    Optional<UserEntity> findBySocialIdAndSocialType(String socialId, User.SocialType socialType);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 이메일 존재 확인
    boolean existsByEmail(String email);

    // 소셜 ID로 사용자 찾기
    @Query("SELECT u FROM UserEntity u WHERE u.socialId = :socialId AND u.socialType = :socialType")
    Optional<UserEntity> findBySocialInfo(@Param("socialId") String socialId,
                                          @Param("socialType") User.SocialType socialType);

}
