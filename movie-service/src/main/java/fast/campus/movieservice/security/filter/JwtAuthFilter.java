package fast.campus.movieservice.security.filter;

import fast.campus.movieservice.security.UsernamePasswordAuthentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// 우리가 만든 Controller 의 endpoint 들은 이 Filter 를 사용하여 인증을 하게 됨.

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.signing-key}")
    private String signingKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = request.getHeader("Authorization");

        SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));

        Claims payload = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        String username = String.valueOf(payload.get("username"));

        GrantedAuthority authority = new SimpleGrantedAuthority("user");

        UsernamePasswordAuthentication authentication = new UsernamePasswordAuthentication(username, null, List.of(authority));

        SecurityContextHolder.getContext().setAuthentication(authentication);;

        filterChain.doFilter(request, response);
    }

    //    login 일 때는 이 필터를 사용하지 않겠다. => jwt token 인증을 하지 않음.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/login");
    }
}
