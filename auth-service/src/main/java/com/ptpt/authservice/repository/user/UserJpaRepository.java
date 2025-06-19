package com.ptpt.authservice.repository.user;

import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    // ===== 기본 조회 메서드 =====

    /**
     * 이메일로 사용자 조회
     */
    Optional<UserEntity> findUserEntityByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     */
    Optional<UserEntity> findByNickname(String nickname);

    /**
     * 소셜 ID와 타입으로 사용자 조회
     */
    Optional<UserEntity> findBySocialIdAndSocialType(String socialId, User.SocialType socialType);

    // ===== 존재 여부 확인 메서드 =====

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     */
    boolean existsByNickname(String nickname);

    /**
     * 소셜 ID와 타입으로 존재 여부 확인
     */
    boolean existsBySocialIdAndSocialType(String socialId, User.SocialType socialType);

    // ===== 집계 및 통계 메서드 =====

    /**
     * 활성화된 사용자 수 조회
     */
    long countByEnabledTrue();

    /**
     * 소셜 타입별 사용자 수 조회
     */
    @Query("SELECT u.socialType, COUNT(u) FROM UserEntity u WHERE u.socialType IS NOT NULL GROUP BY u.socialType")
    List<Object[]> countBySocialType();

    /**
     * 특정 기간 내 가입한 사용자 수 조회
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ===== 페이징 조회 메서드 =====

    /**
     * 활성화된 사용자 페이징 조회
     */
    Page<UserEntity> findByEnabledTrue(Pageable pageable);

    /**
     * 소셜 타입별 사용자 페이징 조회
     */
    Page<UserEntity> findBySocialType(User.SocialType socialType, Pageable pageable);

    /**
     * 닉네임 검색 (부분 일치)
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserEntity> searchByNickname(@Param("keyword") String keyword, Pageable pageable);

    // ===== 벌크 업데이트 메서드 =====

//    /**
//     * 마지막 로그인 시간 업데이트
//     */
//    @Modifying
//    @Query("UPDATE UserEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
//    void updateLastLoginAt(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);
//
//    /**
//     * 사용자 활성화 상태 변경
//     */
//    @Modifying
//    @Query("UPDATE UserEntity u SET u.enabled = :enabled WHERE u.id = :userId")
//    void updateEnabledStatus(@Param("userId") Long userId, @Param("enabled") boolean enabled);
//
//    // ===== 복잡한 조회 메서드 =====
//
//    /**
//     * 이메일 또는 닉네임으로 사용자 조회
//     */
//    @Query("SELECT u FROM UserEntity u WHERE u.email = :emailOrNickname OR u.nickname = :emailOrNickname")
//    Optional<UserEntity> findByEmailOrNickname(@Param("emailOrNickname") String emailOrNickname);
//
//    /**
//     * 특정 기간 동안 로그인하지 않은 사용자 조회 (휴면 계정 후보)
//     */
//    @Query("SELECT u FROM UserEntity u WHERE u.lastLoginAt < :inactiveDate AND u.enabled = true")
//    List<UserEntity> findInactiveUsers(@Param("inactiveDate") LocalDateTime inactiveDate);
//
//    /**
//     * 전화번호로 사용자 조회 (소셜 로그인 연동 확인용)
//     */
//    @Query("SELECT u FROM UserEntity u WHERE u.phoneNumber = :phoneNumber AND u.phoneNumber IS NOT NULL")
//    List<UserEntity> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
//
//    // ===== 삭제 관련 메서드 =====
//
//    /**
//     * 소프트 삭제 (실제 삭제 대신 비활성화)
//     */
//    @Modifying
//    @Query("UPDATE UserEntity u SET u.enabled = false, u.deletedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
//    void softDelete(@Param("userId") Long userId);
//
//    /**
//     * 특정 기간 이전에 삭제된 사용자 영구 삭제
//     */
//    @Modifying
//    @Query("DELETE FROM UserEntity u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :beforeDate")
//    void deleteExpiredUsers(@Param("beforeDate") LocalDateTime beforeDate);
}