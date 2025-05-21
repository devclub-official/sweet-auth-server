package com.ptpt.authservice.config;

import com.ptpt.authservice.filter.JwtAuthenticationFilter;
import com.ptpt.authservice.exception.handler.CustomAccessDeniedHandler;
import com.ptpt.authservice.exception.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.csrf(AbstractHttpConfigurer::disable);
//        httpSecurity.authorizeHttpRequests(c -> {
//            c.anyRequest().permitAll();
//        });
//        return httpSecurity.build();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 기반이므로 세션 사용 안 함
        );

        http.authorizeHttpRequests(c -> {
            c.requestMatchers("/kakao-login").permitAll();
            c.requestMatchers("/swagger-ui/**").permitAll(); // swagger 관련 endpoint 허용
//            c.requestMatchers("/static/**").permitAll();
            c.requestMatchers("/v3/api-docs/**").permitAll();
            c.requestMatchers("/auth/**").permitAll();  // 인증 관련 엔드포인트 허용
            c.requestMatchers(HttpMethod.POST, "/api/users").permitAll();  // 회원가입만 허용
            c.anyRequest().authenticated();
        });

        http.exceptionHandling(except -> {
            except.authenticationEntryPoint(customAuthenticationEntryPoint);
            except.accessDeniedHandler(customAccessDeniedHandler);
        });

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
