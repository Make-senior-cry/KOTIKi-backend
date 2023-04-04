package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.PostService;

@RestController
@Slf4j
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    @Autowired
    public PostController(PostService postService, JwtUtil jwtUtil) {
        this.postService = postService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/post")
    public Mono<ResponseEntity<Object>> createPost(ServerWebExchange swe,
                                                   @RequestPart(required = false) String text,
                                                   @RequestPart(required = false) Mono<FilePart> imageFile) {
        String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();
        return imageFile.flatMap(i -> savePost(text, imageFile, email))
                .switchIfEmpty(Mono.defer(() -> {
                    if (text == null) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return savePost(text, imageFile, email);
                }));
    }

    private Mono<ResponseEntity<Object>> savePost(String text, Mono<FilePart> imageFile, String email) {
        postService.createPost(text, imageFile, email);
        return Mono.just(ResponseEntity.ok().build());
    }
}
