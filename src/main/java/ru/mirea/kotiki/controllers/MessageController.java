package ru.mirea.kotiki.controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.ChatMessage;
import ru.mirea.kotiki.security.JwtUtil;
import ru.mirea.kotiki.services.MessageService;

import java.time.Duration;

@RestController
@RequestMapping("/msg")
@Slf4j
public class MessageController {

    private final MessageService msgService;
    private final JwtUtil jwtUtil;

    @Autowired
    public MessageController(MessageService msgService, JwtUtil jwtUtil) {
        this.msgService = msgService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping("")
    public Mono<String> health(){
        log.info("BOOP");
        return Mono.just("BOOP");
    }


    @PostMapping("")
    public Mono<ResponseEntity<Object>> send(ServerWebExchange swe, @RequestBody ChatMessage msg){
        String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();
        return Mono.just(ResponseEntity.ok(msgService.saveMsg(msg, email)));
    }

    @GetMapping(path = "/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> receiveNewMessages(ServerWebExchange swe, @RequestParam Long senderId){
        String email = jwtUtil.getClaimsFromAccessToken(jwtUtil.extractAccessToken(swe)).getSubject();

        return Flux.interval(Duration.ofMillis(1000))
                .flatMap(s -> msgService.receiveNewFromSender(senderId, email));
    }

}
