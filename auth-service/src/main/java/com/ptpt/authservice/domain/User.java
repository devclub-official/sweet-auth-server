package com.ptpt.authservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ptpt.authservice.entity.user.UserEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// SpringSecurity 관련 데이터를 넘어준다.

@Getter
@Builder
public class User implements UserDetails {

    private Long id;
    private String email;

    @JsonIgnore
    private String password;
    private String username;
    private String profileImage;


    // 실제 사용자의 표시 이름을 반환하는 메서드
    public String getNickname() {
        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
//        return String.valueOf(this.id);
        return this.email;
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(id)
                .username(username)
                .email(email)
                .build();
    }
}
