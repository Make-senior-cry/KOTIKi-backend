package ru.mirea.kotiki.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mirea.kotiki.domain.User;

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

    public String extractUsername(String accessToken) {
        return getClaimsFromAccessToken(accessToken)
                .getSubject();
    }

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

    public String generateAccessToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(accessExpiration)
                .signWith(Keys.hmacShaKeyFor(jwtAccessSecret.getBytes()))
                .claim("role", List.of(user.getRole()))
                .claim("email", user.getEmail())
                .compact();
    }

    public String generateRefreshToken(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(refreshExpiration)
                .signWith(Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes()))
                .compact();
    }
}
