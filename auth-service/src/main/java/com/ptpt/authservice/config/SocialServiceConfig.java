package com.ptpt.authservice.config;

import com.ptpt.authservice.enums.SocialProvider;
import com.ptpt.authservice.service.SocialService;
import com.ptpt.authservice.service.impl.AppleService;
import com.ptpt.authservice.service.impl.KakaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class SocialServiceConfig {

    @Bean
    public Map<SocialProvider, SocialService> socialServices(
            KakaoService kakaoService,
//            NaverService naverService,
            AppleService appleService
    ) {

        Map<SocialProvider, SocialService> services = new EnumMap<>(SocialProvider.class);
        services.put(SocialProvider.KAKAO, kakaoService);
//        services.put(SocialProvider.NAVER, naverService);
        services.put(SocialProvider.APPLE, appleService);

        return services;
    }
}

