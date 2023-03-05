package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.security.WebSecurityConfig;
import ru.mirea.kotiki.services.UserDetailsService;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final static ResponseEntity<Object> UNAUTHORIZED =
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/sign-in")
    public Mono<ResponseEntity> login(ServerWebExchange swe) {
        log.info("Auth started");
        return swe.getFormData().flatMap(credentials ->
                userDetailsService.findByUsername(credentials.getFirst("email"))
                        .cast(User.class)
                        .map(userDetails -> {
                                    if (WebSecurityConfig.passwordEncoder().encode(credentials.getFirst("password"))
                                            .equals(userDetails.getPassword())
                                    ) {
                                        swe.getResponse()
                                                .addCookie(ResponseCookie
                                                        .from("jwt", "Bearer " +
                                                                jwtUtil.generateToken(userDetails))
                                                        .httpOnly(true)
                                                        .build());
                                        log.info("Cookies set");
                                        return ResponseEntity.ok().build();
                                    } else {
                                        log.info("Bad credentials");
                                        return UNAUTHORIZED;
                                    }
                                })
                        .defaultIfEmpty(UNAUTHORIZED)
        );
    }
}
