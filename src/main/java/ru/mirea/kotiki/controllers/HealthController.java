package ru.mirea.kotiki.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

    @GetMapping("/healthcheck")
    public Mono<ResponseEntity<String>> healthcheck(){
        return Mono.just(ResponseEntity.ok("I'm alive"));
    }

}
