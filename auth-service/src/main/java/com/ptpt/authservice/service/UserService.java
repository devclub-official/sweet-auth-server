package com.ptpt.authservice.service;

import com.ptpt.authservice.controller.request.UserUpdateRequestBody;
import com.ptpt.authservice.dto.User;
import com.ptpt.authservice.repository.auth.AuthRepository;
import com.ptpt.authservice.repository.user.UserJpaRepository;
import com.ptpt.authservice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    public User createNewUser(String email, String password, String nickname) {
        // 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }

        User newUser = User.createNormalUser(email, nickname, password);

        return userRepository.save(newUser);
    }

    // 소셜 사용자 생성
    public User createSocialUser(String email, String nickname, String socialId,
                                 User.SocialType socialType, String profileImageUrl, String phoneNumber) {
        // 중복 확인
        if (userRepository.existsByEmail(email)) {
            // 이미 소셜 로그인으로 가입된 사용자인지 확인
            Optional<User> existingUser = userRepository.findBySocialInfo(socialId, socialType);
            if (existingUser.isPresent()) {
                return existingUser.get(); // 이미 존재하는 소셜 사용자
            }
            throw new IllegalArgumentException("다른 방식으로 가입된 이메일입니다");
        }

        User newUser = User.createSocialUser(email, nickname, socialId, socialType, profileImageUrl, phoneNumber);
        return userRepository.save(newUser);
    }

    @Transactional
    // 사용자 정보 업데이트 메서드 추가
    public User updateUserInfo(String email, UserUpdateRequestBody updateRequestBody, MultipartFile profileImage) {
        // 이메일로 사용자 존재 여부 확인
        User user = getUserByEmail(email);

        Long userId = authRepository.getUserByUserId(email).getId();

        // 프로필 이미지가 있는 경우 업로드 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImagePath = saveProfileImage(userId, profileImage);
            updateRequestBody.setProfileImage(profileImagePath);
        }

        // 사용자 정보 업데이트
        return authRepository.updateUser(email, updateRequestBody);
    }

    /**
     * 프로필 이미지 저장 처리
     */
    private String saveProfileImage(Long userId, MultipartFile profileImage) {
        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명 생성 (고유한 파일명을 위해 UUID 사용)
            String originalFileName = profileImage.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 내부적으로 사용자 ID 기반 디렉토리 구조 생성 (URL에 노출되지 않음)
            String userIdStr = userId.toString();

            // 사용자 ID를 기반으로 하위 디렉토리 구조 생성
            Path userPath = Paths.get(uploadDir, userIdStr);
            if (!Files.exists(userPath)) {
                Files.createDirectories(userPath);
            }

            // 파일 저장
//            Path filePath = userPath.resolve(fileName);
//            Files.copy(profileImage.getInputStream(), filePath);

            // 파일 저장
            Path filePath = userPath.resolve(fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("프로필 이미지 저장 완료 - 사용자: {}, 파일: {}", userId, fileName);

            // URL에는 사용자 ID를 포함시키고 파일명도 함께 반환
            return "/images/profiles/" + userIdStr + "/" + fileName;
        } catch (IOException e) {
            log.error("프로필 이미지 저장 실패", e);
            throw new RuntimeException("프로필 이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("서버 에러: " + e.getMessage());
        }
    }


    /**
     * 이메일로 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findBySocialInfo(String socialId, User.SocialType socialType) {
        return userRepository.findBySocialInfo(socialId, socialType);
    }
}
