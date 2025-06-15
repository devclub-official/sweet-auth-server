package com.ptpt.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.enums.ApiResponseCode;
import com.ptpt.authservice.exceptions.AuthServiceException;
import com.ptpt.authservice.exceptions.social.SocialEmailAlreadyExistsException;
import com.ptpt.authservice.exceptions.user.UserNotFoundException;
import com.ptpt.authservice.exceptions.user.UserCreateFailedException;
import com.ptpt.authservice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageService profileImageService;
    private final ObjectMapper objectMapper;

    // ===== User Creation Methods =====

    /**
     * 일반 사용자 생성
     */
    @Transactional
    public User createNormalUser(String email, String password, String nickname) {
        validateNewUserInput(email, nickname);

        String encodedPassword = passwordEncoder.encode(password);
        User newUser = User.createNormalUser(email, nickname, encodedPassword);

        try {
            User savedUser = userRepository.save(newUser);
            log.info("일반 사용자 생성 완료 - userId: {}, email: {}", savedUser.getId(), email);
            return savedUser;
        } catch (Exception e) {
            log.error("사용자 생성 중 오류 발생", e);
            throw new UserCreateFailedException("사용자 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 소셜 사용자 생성 (User DTO 팩토리 메서드 사용)
     */
    @Transactional
    public User createSocialUser(String email,
                                 String nickname,
                                 String socialId,
                                 User.SocialType socialType,
                                 String socialProfileImageUrl,
                                 String phoneNumber,
                                 LocalDate birthDate,
                                 String location,
                                 String interestedSportsJson,
                                 String profileImageUrl,
                                 String bio) {

        log.info("소셜 사용자 생성 시작 - email: {}, socialType: {}", email, socialType);

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new UserCreateFailedException("이미 가입된 이메일입니다: " + email);
        }

        // 2. 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new UserCreateFailedException("이미 존재하는 닉네임입니다: " + nickname);
        }

        // 3. 소셜 계정 중복 확인
        if (userRepository.existsBySocialIdAndSocialType(socialId, socialType)) {
            throw new UserCreateFailedException("이미 가입된 소셜 계정입니다");
        }

        try {
            // 4. User DTO 팩토리 메서드를 사용하여 사용자 생성
            User user = User.createSocialUser(
                    email, nickname, socialId, socialType, socialProfileImageUrl,
                    phoneNumber, birthDate, location, interestedSportsJson, profileImageUrl, bio
            );

            // 5. 엔티티로 변환하여 저장 (실제 구현에서는 UserEntity에도 새 필드들이 추가되어야 함)
            User savedUser = userRepository.save(user); // 실제로는 Entity 변환 후 저장
            log.info("소셜 사용자 생성 완료 - userId: {}", savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            log.error("소셜 사용자 생성 실패 - email: {}", email, e);
            throw new UserCreateFailedException("사용자 계정 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 관심 스포츠 JSON을 List로 변환하는 유틸리티 메서드
     */
    public List<String> parseInterestedSports(String interestedSportsJson) {
        if (interestedSportsJson == null || interestedSportsJson.isEmpty()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(interestedSportsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("관심 스포츠 JSON 파싱 실패: {}", interestedSportsJson, e);
            return List.of();
        }
    }

    // ===== User Update Methods =====

    /**
     * 사용자 정보 업데이트
     */
    @Transactional
    public User updateUserInfo(String email, UserUpdateRequestBody updateRequest, MultipartFile profileImage) {
        log.info("사용자 정보 업데이트 요청 - email: {}", email);

        User user = getUserByEmail(email);

        if (updateRequest.getNickname() != null && !updateRequest.getNickname().equals(user.getNickname())) {
            validateNicknameAvailable(updateRequest.getNickname());
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = profileImageService.saveProfileImage(user.getId(), profileImage);
                updateRequest.setProfileImage(imageUrl);
            } catch (Exception e) {
                log.error("프로필 이미지 저장 중 오류 발생", e);
                throw new AuthServiceException(ApiResponseCode.USER_UPDATE_FAILED, "프로필 이미지 저장 중 오류가 발생했습니다.");
            }
        }

        try {
            user.updateUserInfo(
                    updateRequest.getNickname(),
                    updateRequest.getPhoneNumber(),
                    updateRequest.getProfileImage()
            );
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류 발생", e);
            throw new AuthServiceException(ApiResponseCode.USER_UPDATE_FAILED, "사용자 정보 업데이트 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = authenticateUser(email, currentPassword);

        if (user.isSocialUser()) {
            throw new AuthServiceException(ApiResponseCode.AUTH_LOGIN_FAILED, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthServiceException(ApiResponseCode.AUTH_LOGIN_FAILED, "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);

        userRepository.save(user);
        log.info("비밀번호 변경 완료 - userId: {}", user.getId());
    }

    // ===== User Query Methods =====

    /**
     * 이메일로 사용자 조회
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. email: " + email));
    }

    /**
     * ID로 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    /**
     * 이메일로 사용자 조회 (Optional)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 소셜 정보로 사용자 조회
     */
    public Optional<User> findBySocialInfo(String socialId, User.SocialType socialType) {
        return userRepository.findBySocialIdAndSocialType(socialId, socialType);
    }

    /**
     * 닉네임으로 사용자 조회
     */
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    // ===== Authentication Methods =====

    /**
     * 이메일과 비밀번호로 사용자 인증
     */
    public User authenticateUser(String email, String password) {
        User user = getUserByEmail(email);

        log.debug("UserService email {}", user.getEmail());

        if (user.isSocialUser()) {
            throw new AuthServiceException(ApiResponseCode.AUTH_LOGIN_FAILED, "소셜 로그인 사용자입니다.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthServiceException(ApiResponseCode.AUTH_LOGIN_FAILED, "비밀번호가 올바르지 않습니다.");
        }

        if (!user.isEnabled()) {
            throw new AuthServiceException(ApiResponseCode.AUTH_LOGIN_FAILED, "비활성화된 계정입니다.");
        }

        return user;
    }

    // ===== Validation Methods =====

    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 소셜 ID와 타입으로 존재 여부 확인
     */
    public boolean existsBySocialIdAndProvider(String socialId, String provider) {
        User.SocialType socialType = User.SocialType.valueOf(provider.toUpperCase());
        return userRepository.existsBySocialIdAndSocialType(socialId, socialType);
    }

    /**
     * 소셜 정보로 사용자 ID 조회
     */
    public String findUserIdBySocialInfo(String socialId, String provider) {
        User.SocialType socialType = User.SocialType.valueOf(provider.toUpperCase());
        User user = userRepository.findBySocialIdAndSocialType(socialId, socialType)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        return String.valueOf(user.getId());
    }

    // ===== Private Validation Methods =====

    private void validateNewUserInput(String email, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new SocialEmailAlreadyExistsException(email);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new UserCreateFailedException("이미 존재하는 닉네임입니다: " + nickname);
        }
    }

    private void validateSocialUserInput(String socialId, User.SocialType socialType) {
        if (userRepository.existsBySocialIdAndSocialType(socialId, socialType)) {
            throw new UserCreateFailedException("이미 가입된 소셜 계정입니다.");
        }
    }

    private void validateNicknameAvailable(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new UserCreateFailedException("이미 존재하는 닉네임입니다: " + nickname);
        }
    }
}