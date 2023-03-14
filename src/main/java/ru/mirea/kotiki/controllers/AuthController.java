package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.UserDetailsService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final static ResponseEntity<Object> OK = ResponseEntity.ok().build();

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-up")
    public Mono<ResponseEntity<UserDto>> register(ServerWebExchange swe, @RequestBody User user) {
        log.info("User registration with email \"" + user.getEmail() + "\" started");
        return Mono.just(user)
                .cast(User.class)
                .flatMap(u -> userDetailsService.register(user))
                .doOnNext(u -> jwtUtil.setCookies(swe.getResponse(), u))
                .flatMap(u -> Mono.just(new UserDto(u)))
                .flatMap(u -> Mono.just(ResponseEntity.ok(u)))
                .defaultIfEmpty(ResponseEntity.badRequest().body(new UserDto()));
    }

    @PostMapping("/sign-in")
    public Mono<ResponseEntity<UserDto>> login(ServerWebExchange swe, @RequestBody Map<String, String> credentials) {
        log.info("User authentication with email \"" + credentials.get("email") + "\" started");
        return userDetailsService.findByUsername(credentials.get("email")).cast(User.class).map(u -> {
            if (passwordEncoder.matches(credentials.get("password"), u.getPassword())) {
                jwtUtil.setCookies(swe.getResponse(), u);
                log.info("Cookies set successfully");
                return ResponseEntity.ok(new UserDto(u));
            } else {
                log.info("Bad credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserDto());
            }
        }).defaultIfEmpty(ResponseEntity.badRequest().body(new UserDto()));
    }

    @PostMapping("/sign-out")
    public Mono<ResponseEntity<Object>> signOut(ServerWebExchange swe) {
        jwtUtil.removeCookies(swe.getResponse());
        return Mono.just(OK);
    }
}
