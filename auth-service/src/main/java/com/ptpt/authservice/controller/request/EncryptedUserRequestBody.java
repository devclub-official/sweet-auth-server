package com.ptpt.authservice.controller.request;

import com.ptpt.authservice.annotation.PasswordEncryption;
import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class EncryptedUserRequestBody {

    private final String email;

    @PasswordEncryption
    private final String password;

    private final String username;

//    https://kdohyeon.tistory.com/97
    @ConstructorProperties({"email", "password", "username"})
    public EncryptedUserRequestBody(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
}
