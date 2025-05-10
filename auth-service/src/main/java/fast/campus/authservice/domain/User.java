package fast.campus.authservice.domain;

import fast.campus.authservice.entity.user.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// SpringSecurity 관련 데이터를 넘어준다.

@Getter
public class User implements UserDetails {

    private final String email;
    private final String password;
    private final String username;

    public User(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public UserEntity toEntity() {
        return new UserEntity(email, password, username);
    }
}
