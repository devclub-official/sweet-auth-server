package com.ptpt.authservice.exception.auth;

public class InvalidAuthException extends RuntimeException{
    public InvalidAuthException() {
        super("사용자 ID 또는 비밀번호가 일치하지 않습니다.");
    }
}
