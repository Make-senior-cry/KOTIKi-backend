package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.UserDetailsService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final static ResponseEntity<Object> UNAUTHORIZED = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    private final static ResponseEntity<Object> BAD_REQUEST = ResponseEntity.badRequest().build();
    private final static ResponseEntity<Object> OK = ResponseEntity.ok().build();

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserDetailsService userDetailsService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/sign-up")
    public Mono<ResponseEntity<String>> register(@RequestBody User user) {
        log.info("Registration started");
        return Mono.just(user)
                .flatMap(u -> userDetailsService.register(user))
                .flatMap(u -> Mono.just(ResponseEntity.ok().body("SUCCESS")))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().body("FAIL")));
    }

    @PostMapping("/sign-in")
    public Mono<ResponseEntity<Object>> login(ServerWebExchange swe, @RequestBody Map<String, String> credentials) {
        log.info("User authentication with email \"" + credentials.get("email") + "\" started");
        return userDetailsService.findByUsername(credentials.get("email")).cast(User.class).map(u -> {
            if (passwordEncoder.matches(credentials.get("password"), u.getPassword())) {
                swe.getResponse()
                        .addCookie(ResponseCookie
                                .from("access-token", jwtUtil.generateToken(u))
                                .httpOnly(true)
                                .build());
                log.info("Cookies set successfully");
                return OK;
            } else {
                log.info("Bad credentials");
                return UNAUTHORIZED;
            }
        }).defaultIfEmpty(BAD_REQUEST);
    }
}
