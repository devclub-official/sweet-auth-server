package fast.campus.movieservice.config;

import fast.campus.movieservice.security.filter.InitAuthFilter;
import fast.campus.movieservice.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final InitAuthFilter initAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.addFilterBefore(initAuthFilter, BasicAuthenticationFilter.class);
        httpSecurity.addFilterAfter(jwtAuthFilter, BasicAuthenticationFilter.class);


        httpSecurity.authorizeHttpRequests(c -> c.anyRequest().authenticated());
        return httpSecurity.build();
    }
}
