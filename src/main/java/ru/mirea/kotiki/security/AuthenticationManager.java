package ru.mirea.kotiki.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        log.info("Auth manager started");
        String accessToken = authentication.getCredentials().toString();
        String refreshToken = authentication.getPrincipal().toString();

        try {
            jwtUtil.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            return Mono.empty();
        }

        Claims claims;
        try {
            claims = jwtUtil.getClaimsFromAccessToken(accessToken);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }

        String username = claims.getSubject();
        List<String> role = claims.get("role", List.class);

        List<SimpleGrantedAuthority> authorities = role.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        return Mono.just(authenticationToken);
    }
}