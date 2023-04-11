package ru.mirea.kotiki.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.FeedDto;
import ru.mirea.kotiki.dto.UserPageDto;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.PostService;
import ru.mirea.kotiki.utils.FeedType;

import java.util.Map;

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

    @GetMapping("/post")
    public Mono<ResponseEntity<UserPageDto>> getPosts(@RequestParam("user_id") Long userId, @RequestParam Integer limit,
                                                      @RequestParam Integer skip) {
        return postService.loadUserPage(userId, limit, skip)
                .flatMap(p -> Mono.just(ResponseEntity.ok(p)));
    }

    @PostMapping("/post/like")
    public Mono<ResponseEntity<Object>> likePost(ServerWebExchange swe, @RequestBody Map<String, Long> body) {
        String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();
        return postService.likePost(email, body.get("postId"))
                .flatMap(c -> Mono.just(ResponseEntity.ok(Map.of("likesCount", c))));
    }

    @PostMapping("/post/ban")
    public Mono<ResponseEntity<Void>> banPost(@RequestBody Map<String, Long> body) {
        return postService.banPost(body.get("postId"))
                .flatMap(v -> Mono.just(ResponseEntity.ok().build()));
    }

    @PostMapping("/post/report")
    public Mono<ResponseEntity<Object>> reportPost(ServerWebExchange swe, @RequestBody Map<String, Long> body) {
        String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();
        return postService.reportPost(email, body.get("postId"))
                .flatMap(flag -> {
                    if (flag)
                        return Mono.just(ResponseEntity.ok().build());
                    else
                        return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @GetMapping("/feed")
    public Mono<ResponseEntity<FeedDto>> getFeed(ServerWebExchange swe,
                                                 @RequestParam("type") FeedType type,
                                                 Integer skip, Integer limit) {
        if (type == FeedType.NEW) {
            return postService.getNewPosts(skip, limit).flatMap(r -> Mono.just(ResponseEntity.ok(r)));
        } else if (type == FeedType.FOLLOWING) {
            String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();
            return postService.getFollowingPosts(email, skip, limit).flatMap(r -> Mono.just(ResponseEntity.ok(r)));
        } else {
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }
}
