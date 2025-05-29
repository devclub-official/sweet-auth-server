-- resources/sql/schema.sql
-- 통합 스키마 방식: 일반 사용자와 소셜 사용자를 하나의 테이블에서 관리
-- 소셜 토큰은 저장하지 않고 로그인 완료 후 즉시 폐기

-- DROP TABLE IF EXISTS users;

CREATE TABLE users (
    -- 기본 식별자
   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '사용자 고유 ID',

    -- 공통 사용자 정보
   email VARCHAR(255) NOT NULL UNIQUE COMMENT '사용자 이메일 (로그인 ID로 사용)',
   nickname VARCHAR(30) NOT NULL UNIQUE COMMENT '사용자 닉네임 (표시명)',

    -- 일반 로그인 사용자 전용 (소셜 사용자는 NULL)
   password VARCHAR(255) NULL COMMENT '암호화된 비밀번호 (소셜 사용자는 NULL)',

    -- 프로필 정보
   bio VARCHAR(150) NULL COMMENT '사용자 소개',
   profile_image VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
   phone_number VARCHAR(20) NULL COMMENT '전화번호',

    -- 소셜 로그인 정보 (일반 사용자는 NULL)
   social_id VARCHAR(100) NULL COMMENT '소셜 플랫폼 사용자 ID',
   social_type VARCHAR(20) NULL COMMENT '소셜 플랫폼 타입 (KAKAO, GOOGLE, NAVER)',
   social_profile_image VARCHAR(500) NULL COMMENT '소셜에서 가져온 원본 프로필 이미지 URL',

    -- 사용자 타입 구분
   user_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '사용자 타입 (NORMAL: 일반가입, SOCIAL: 소셜가입)',

    -- 계정 상태
   is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 상태',
   is_email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '이메일 인증 상태',

    -- 개인정보 동의
   agree_terms BOOLEAN NULL DEFAULT FALSE COMMENT '서비스 이용약관 동의',
   agree_privacy BOOLEAN NULL DEFAULT FALSE COMMENT '개인정보처리방침 동의',
   agree_marketing BOOLEAN NULL DEFAULT FALSE COMMENT '마케팅 정보 수신 동의 (선택)',

    -- 마지막 로그인 정보
   last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 시간',
   last_login_ip VARCHAR(45) NULL COMMENT '마지막 로그인 IP',

    -- 시간 정보
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일시',
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정일시',

    -- 인덱스 설정
   INDEX idx_users_email (email) COMMENT '이메일 조회 최적화',
   INDEX idx_users_nickname (nickname) COMMENT '닉네임 조회 최적화',
   INDEX idx_users_social_info (social_id, social_type) COMMENT '소셜 정보 조회 최적화',
   INDEX idx_users_user_type (user_type) COMMENT '사용자 타입별 조회 최적화',
   INDEX idx_users_created_at (created_at) COMMENT '가입일시 정렬 최적화',
   INDEX idx_users_last_login (last_login_at) COMMENT '마지막 로그인 정렬 최적화',

    -- 제약 조건
--    CONSTRAINT chk_users_user_type CHECK (user_type IN ('NORMAL', 'SOCIAL')),
--    CONSTRAINT chk_users_social_type CHECK (social_type IS NULL OR social_type IN ('KAKAO', 'GOOGLE', 'NAVER')),
--    CONSTRAINT chk_users_password_required CHECK (
--        (user_type = 'NORMAL' AND password IS NOT NULL) OR
--        (user_type = 'SOCIAL' AND password IS NULL)
--        ),
--    CONSTRAINT chk_users_social_info_required CHECK (
--        (user_type = 'NORMAL' AND social_id IS NULL AND social_type IS NULL) OR
--        (user_type = 'SOCIAL' AND social_id IS NOT NULL AND social_type IS NOT NULL)
--        ),

    -- 소셜 계정 중복 방지 (같은 소셜 플랫폼의 같은 ID는 하나만 존재)
   UNIQUE KEY uk_users_social_account (social_id, social_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보 통합 테이블';

-- 데이터 무결성을 위한 추가 체크사항들을 주석으로 설명
/*
무결성 규칙 설명:

1. 이메일 중복 방지:
   - 일반 사용자와 소셜 사용자 간에도 같은 이메일 사용 불가
   - 하나의 이메일은 하나의 계정에만 연결

2. 소셜 계정 중복 방지:
   - 같은 소셜 플랫폼의 같은 사용자 ID는 하나의 계정에만 연결
   - 예: 카카오 ID "123456"은 하나의 사용자 계정에만 연결 가능

3. 사용자 타입별 필수 필드:
   - NORMAL: password 필수, social_id/social_type NULL
   - SOCIAL: password NULL, social_id/social_type 필수

4. 프로필 이미지 우선순위:
   - profile_image: 사용자가 직접 업로드한 이미지 (우선순위 1)
   - social_profile_image: 소셜에서 가져온 이미지 (백업용, 우선순위 2)
   - 애플리케이션 로직에서 profile_image가 없으면 social_profile_image 사용

5. 계정 상태 관리:
   - is_active: 계정 활성화/비활성화 (관리자 제재 등)
   - is_email_verified: 이메일 인증 상태 (소셜 로그인은 자동 TRUE)
*/