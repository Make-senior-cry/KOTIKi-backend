package ru.mirea.kotiki.controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.ChatMessage;
import ru.mirea.kotiki.repositories.ChatMessageRepository;
import ru.mirea.kotiki.services.PubSubService;

@Controller
@Slf4j
public class MessageController {

    private final ChatMessageRepository messageRepository;
    private final PubSubService pubSubService;

    @Autowired
    public MessageController(ChatMessageRepository messageRepository, PubSubService pubSubService) {
        this.messageRepository = messageRepository;
        this.pubSubService = pubSubService;
    }

    //TEST
    @MessageMapping("test")
    public Flux<String> getAllMessages(ServerWebExchange swe){
        return messageRepository.getAllSent(1).map(ChatMessage::getText);
    }

    @MessageMapping("send")
    public Mono<Void> send(ChatMessage message){
        return pubSubService.publish(message);
    }

    @MessageMapping("subscribe")
    public Flux<ChatMessage> subscribe(){
        return pubSubService.subscribe();
    }
}
