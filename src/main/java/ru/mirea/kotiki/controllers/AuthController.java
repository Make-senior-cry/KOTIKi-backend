package ru.mirea.kotiki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.UserDetailsService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final static ResponseEntity<Object> UNAUTHORIZED =
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserDetailsService userDetailsService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/sign-in")
    public Mono<ResponseEntity> login(ServerWebExchange swe) {
        return swe.getFormData().flatMap(credentials ->
                userDetailsService.findByUsername(credentials.getFirst("email"))
                        .cast(User.class)
                        .map(userDetails -> {
                                    if (passwordEncoder.encode(credentials.getFirst("password")).equals(
                                            userDetails.getPassword()
                                    )) {
                                        swe.getResponse()
                                                .addCookie(ResponseCookie
                                                        .from("jwt", "Bearer " +
                                                                jwtUtil.generateToken(userDetails))
                                                        .httpOnly(true)
                                                        .build());
                                        return ResponseEntity.ok().build();
                                    } else {
                                        return UNAUTHORIZED;
                                    }
                                })
                        .defaultIfEmpty(UNAUTHORIZED)
        );
    }
}
