package ru.mirea.kotiki.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.UserRole;

import java.util.List;

@Slf4j
@Component
public class JwtWebFilter implements WebFilter {
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtWebFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("Web filter started");

        List<HttpCookie> accessTokenCookies = exchange.getRequest()
                .getCookies()
                .get("access-token");

        List<HttpCookie> refreshTokenCookies = exchange.getRequest()
                .getCookies()
                .get("refresh-token");

        String accessToken, refreshToken;
        if (accessTokenCookies != null && refreshTokenCookies != null) {
            accessToken = extractAccessToken(accessTokenCookies);
            refreshToken = extractRefreshToken(refreshTokenCookies);
        } else {
            return chain.filter(exchange);
        }

        try {
            jwtUtil.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            return chain.filter(exchange);
        }

        try {
            jwtUtil.validateAccessToken(accessToken);
        } catch (ExpiredJwtException e) {
            Claims claims = jwtUtil.getClaimsFromRefreshToken(refreshToken);
            List<String> role = claims.get("role", List.class);

            updateCookie(exchange.getResponse(), claims.getSubject(), UserRole.valueOf(role.get(0)));
        }

        return chain.filter(exchange);
    }

    private String extractAccessToken(List<HttpCookie> accessTokenCookies) {
        return accessTokenCookies.stream()
                .filter(c -> c.getName().equals("access-token"))
                .map(HttpCookie::getValue).findFirst().get();
    }

    private String extractRefreshToken(List<HttpCookie> refreshTokenCookies) {
        return refreshTokenCookies.stream()
                .filter(c -> c.getName().equals("refresh-token"))
                .map(HttpCookie::getValue).findFirst().get();
    }

    private void updateCookie(ServerHttpResponse response, String email, UserRole role) {
        response.addCookie(ResponseCookie
                .from("access-token", jwtUtil.generateAccessToken(email, role))
                .path("/")
                .httpOnly(true)
                .build());
        response.addCookie(ResponseCookie
                .from("refresh-token", jwtUtil.generateRefreshToken(email, role))
                .path("/")
                .httpOnly(true)
                .build());
        log.info("Cookies updated");
    }
}
