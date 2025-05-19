package com.ptpt.authservice.controller.request;

import com.ptpt.authservice.annotation.PasswordEncryption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
@Schema(description = "사용자 등록 요청")
public class EncryptedUserRequestBody {

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private final String email;

    @PasswordEncryption
    @Schema(description = "사용자 비밀번호 (자동으로 암호화됨)", example = "password123")
    private final String password;

    @Schema(description = "사용자 이름", example = "user")
    private final String username;

//    https://kdohyeon.tistory.com/97
    @ConstructorProperties({"email", "password", "username"})
    public EncryptedUserRequestBody(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
