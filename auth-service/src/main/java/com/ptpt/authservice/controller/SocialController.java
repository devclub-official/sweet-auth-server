package com.ptpt.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SocialController {

    @GetMapping("/kakao-login")
    public String kakao(@RequestParam String code) {
        log.info("code = {}", code);
        return "hello + " + code + "!";
    }
}
