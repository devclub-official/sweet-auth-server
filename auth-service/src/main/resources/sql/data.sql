-- 초기 데이터 삽입 (선택사항)
-- 테스트용 일반 사용자 (비밀번호: "password123")
INSERT INTO users (
    email, nickname, password, user_type,
    is_email_verified, agree_terms, agree_privacy
) VALUES (
     'test@example.com',
     'TestUser',
     '$2a$10$9bPO6EftPG/T5hopX4u/wuH8fOVOgZO3ptcFdpQoAWm9H523PenNK', -- bcrypt 해시된 "password123"
     'NORMAL',
     TRUE,
     TRUE,
     TRUE
 );