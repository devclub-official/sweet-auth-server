package com.ptpt.authservice.controller.request;

import lombok.Getter;

import java.beans.ConstructorProperties;

@Getter
public class SimpleUserRequestBody {

    private final String email;

    private String password;

//    https://kdohyeon.tistory.com/97
    @ConstructorProperties({"email", "password"})
    public SimpleUserRequestBody(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
