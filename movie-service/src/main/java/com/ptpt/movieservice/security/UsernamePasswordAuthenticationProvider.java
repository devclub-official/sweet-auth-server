package com.ptpt.movieservice.security;

import com.ptpt.movieservice.delegator.AuthenticationDelegator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {


//    인증을 할 때 provider 가 직접하는 게 아니라 auth service 의 endpoint 를 호출해서 결과값을 얻어야 한다.
    private final AuthenticationDelegator delegator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        delegator.restAuth(userId, password);

        return new UsernamePasswordAuthentication(userId, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthentication.class.isAssignableFrom(authentication);
    }
}
