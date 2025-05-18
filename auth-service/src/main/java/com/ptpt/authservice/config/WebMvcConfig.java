package com.ptpt.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 이미지 파일에 대한 접근 경로 설정
        registry.addResourceHandler("/images/profiles/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600);  // 캐싱 설정 (1시간)

        // 기본 정적 리소스 설정 유지
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
