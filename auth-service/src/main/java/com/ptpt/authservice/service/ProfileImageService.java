package com.ptpt.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    /**
     * 프로필 이미지 저장
     */
    public String saveProfileImage(Long userId, MultipartFile profileImage) {
        validateImageFile(profileImage);

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = createUserDirectory(userId);

            // 파일명 생성
            String fileName = generateUniqueFileName(profileImage.getOriginalFilename());

            // 기존 프로필 이미지 삭제 (선택적)
            deleteExistingProfileImages(uploadPath);

            // 파일 저장
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("프로필 이미지 저장 완료 - userId: {}, fileName: {}", userId, fileName);

            // 접근 URL 반환
            return buildImageUrl(userId, fileName);

        } catch (IOException e) {
            log.error("프로필 이미지 저장 실패 - userId: {}", userId, e);
            throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
        }
    }

    /**
     * 프로필 이미지 삭제
     */
    public void deleteProfileImage(Long userId, String imageUrl) {
        try {
            String fileName = extractFileName(imageUrl);
            Path filePath = Paths.get(uploadDir, userId.toString(), fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("프로필 이미지 삭제 완료 - userId: {}, fileName: {}", userId, fileName);
            }
        } catch (IOException e) {
            log.error("프로필 이미지 삭제 실패 - userId: {}", userId, e);
            // 삭제 실패는 로그만 남기고 예외를 던지지 않음
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용된 형식: " + ALLOWED_EXTENSIONS);
        }

        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

    /**
     * 사용자별 디렉토리 생성
     */
    private Path createUserDirectory(Long userId) throws IOException {
        Path userPath = Paths.get(uploadDir, userId.toString());
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath);
        }
        return userPath;
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }
        return fileName.substring(lastIndexOfDot);
    }

    /**
     * 이미지 접근 URL 생성
     */
    private String buildImageUrl(Long userId, String fileName) {
        return String.format("%s/%d/%s", accessPath, userId, fileName);
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileName(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        return lastSlashIndex != -1 ? imageUrl.substring(lastSlashIndex + 1) : imageUrl;
    }

    /**
     * 기존 프로필 이미지 삭제 (선택적)
     */
    private void deleteExistingProfileImages(Path userDirectory) {
        try {
            Files.list(userDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                            log.debug("기존 프로필 이미지 삭제: {}", file.getFileName());
                        } catch (IOException e) {
                            log.warn("기존 프로필 이미지 삭제 실패: {}", file.getFileName());
                        }
                    });
        } catch (IOException e) {
            log.warn("기존 프로필 이미지 목록 조회 실패", e);
        }
    }
}