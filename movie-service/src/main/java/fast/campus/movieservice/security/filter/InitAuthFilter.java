package fast.campus.movieservice.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import fast.campus.movieservice.domain.request.LoginRequest;
import fast.campus.movieservice.security.OtpAuthentication;
import fast.campus.movieservice.security.OtpAuthenticationProvider;
import fast.campus.movieservice.security.UsernamePasswordAuthentication;
import fast.campus.movieservice.security.UsernamePasswordAuthenticationProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitAuthFilter extends OncePerRequestFilter {

    private final OtpAuthenticationProvider otpAuthenticationProvider;
    private final UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider;
    private final ObjectMapper objectMapper;

    @Value("${jwt.signing-key}")
    private String jwtKey;

//    login 일 때 이 filter 사용
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

        String username = loginRequest.getUserId();
        String password = loginRequest.getPassword();
        String otp = loginRequest.getOtp();
//
//        String username = request.getHeader("username");
//        String password = request.getHeader("password");
//        String otp = request.getHeader("otp");

        log.info("Username: {}", username);
        log.info("Password: {}", password);
        log.info("OTP: {}", otp);

        if (StringUtils.isBlank(otp)) {
            UsernamePasswordAuthentication authentication = new UsernamePasswordAuthentication(username, password);
            usernamePasswordAuthenticationProvider.authenticate(authentication);
        } else {
            Authentication authentication = new OtpAuthentication(username, otp);
            otpAuthenticationProvider.authenticate(authentication);

            SecretKey secretKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));

            String jwt = Jwts.builder()
                    .claim("username", username)
                    .signWith(secretKey)
                    .compact();

            response.setHeader("Authorization", jwt);
        }

        filterChain.doFilter(request, response);
    }

//    login 이 아닐때는 이 필터를 사용하지 않겠다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().equals("/login");
    }
}
