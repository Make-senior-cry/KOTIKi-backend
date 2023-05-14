package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.UserService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {


    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Mono<UserDto> getUser(ServerWebExchange swe, @RequestParam Optional<Long> id) {
        if (id.isPresent()) {
            return userService.getUser(id.get());
        } else {
            String email = jwtUtil.extractSubject(swe);
            return userService.getUser(email);
        }
    }

    @PutMapping
    public Mono<ResponseEntity<UserDto>> updateUser(ServerWebExchange swe,
                                                    @RequestPart(required = false) String name,
                                                    @RequestPart(required = false) String description) {
        log.info("handling user update");
        return userService.updateUser(jwtUtil.extractSubject(swe), name, description)
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/follow")
    public Mono<ResponseEntity<UserDto>> followUser(ServerWebExchange swe, @RequestBody Map<String, Long> body) {
        return userService.followUser(jwtUtil.extractSubject(swe), body.get("followingId"))
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }

    @GetMapping("/following")
    public Mono<ResponseEntity<Map<String, Boolean>>> checkFollowing(ServerWebExchange swe, @RequestParam Long id) {
        return userService.checkFollowing(jwtUtil.extractSubject(swe), id)
                .flatMap(f -> Mono.just(ResponseEntity.ok().body(Map.of("isFollower", f))))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }
}
