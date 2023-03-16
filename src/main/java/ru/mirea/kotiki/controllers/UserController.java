package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.UserService;

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
    public Mono<UserDto> getCount(@RequestParam Long id){
        return userService.getUser(id);
    }

    @PutMapping
    public Mono<ResponseEntity<UserDto>> updateUser(ServerWebExchange swe,
                                           @RequestPart(required = false) String name,
                                           @RequestPart(required = false) String description,
                                           @RequestPart(required = false) Mono<FilePart> imageFile) {
        return userService.updateUser(jwtUtil
                        .getClaimsFromAccessToken(swe
                                .getRequest()
                                .getCookies()
                                .getFirst("access-token")
                                .getValue())
                        .getSubject()
                ,name,description, imageFile)
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));

    }
}
