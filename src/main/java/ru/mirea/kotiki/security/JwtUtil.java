package ru.mirea.kotiki.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.domain.UserRole;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    @Value("${jwt.access-token.secret}")
    private String jwtAccessSecret;

    @Value("${jwt.refresh-token.secret}")
    private String jwtRefreshSecret;

    public Claims getClaimsFromAccessToken(String accessToken) {
        String key = Base64.getEncoder().encodeToString(jwtAccessSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }

    public Claims getClaimsFromRefreshToken(String refreshToken) {
        String key = Base64.getEncoder().encodeToString(jwtRefreshSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
    }

    public boolean validateAccessToken(String accessToken) {
        return getClaimsFromAccessToken(accessToken)
                .getExpiration()
                .after(new Date());
    }

    public boolean validateRefreshToken(String refreshToken) {
        return getClaimsFromRefreshToken(refreshToken)
                .getExpiration()
                .after(new Date());
    }

    public String generateAccessToken(String email, UserRole role) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);

        return Jwts.builder()
                .setSubject(email)
                .setExpiration(accessExpiration)
                .signWith(Keys.hmacShaKeyFor(jwtAccessSecret.getBytes()))
                .claim("role", List.of(role))
                .compact();
    }

    public String generateRefreshToken() {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);

        return Jwts.builder()
                .setExpiration(refreshExpiration)
                .signWith(Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes()))
                .compact();
    }

    public void setCookies(ServerHttpResponse response, User user) {
        response.addCookie(ResponseCookie
                .from("access-token", generateAccessToken(user.getEmail(), user.getRole()))
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build());
        response.addCookie(ResponseCookie
                .from("refresh-token", generateRefreshToken())
                .path("/")
                .secure(true)
                .sameSite("None")
                .httpOnly(true)
                .build());
    }

    public void removeCookies(ServerHttpResponse response) {
        response.addCookie(ResponseCookie
                .from("access-token", "expired")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .build());
        response.addCookie(ResponseCookie
                .from("refresh-token", "expired")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .build());
    }

    public String extractAccessToken(ServerWebExchange swe) {
        List<ResponseCookie> accessTokenCookies = swe.getResponse()
                .getCookies()
                .get("access-token");

        if (accessTokenCookies == null) {
            return swe.getRequest()
                    .getCookies()
                    .get("access-token")
                    .stream()
                    .filter(c -> c.getName().equals("access-token"))
                    .map(HttpCookie::getValue).findFirst().get();
        } else {
            return accessTokenCookies.stream()
                    .filter(c -> c.getName().equals("access-token"))
                    .map(HttpCookie::getValue).findFirst().get();
        }
    }

    public String extractSubject(ServerWebExchange swe) {
        return getClaimsFromAccessToken(extractAccessToken(swe)).getSubject();
    }
}
