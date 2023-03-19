package ru.mirea.kotiki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.SearchUsersDto;
import ru.mirea.kotiki.services.UserService;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final UserService userService;

    @Autowired
    public SearchController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public Mono<ResponseEntity<SearchUsersDto>> searchUsers(@RequestParam String name,
                                                            @RequestParam Integer skip,
                                                            @RequestParam Integer limit) {
        return userService.searchUsers(name, skip, limit)
                .flatMap(u -> userService.getNext(name, skip, limit)
                        .flatMap(next -> Mono.just(SearchUsersDto
                                .builder()
                                .hasPrev(skip != 0)
                                .hasNext(next)
                                .skip(skip)
                                .limit(limit)
                                .users(u)
                                .build())
                        )
                )
                .flatMap(dto -> Mono.just(ResponseEntity.ok(dto)))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }
}
