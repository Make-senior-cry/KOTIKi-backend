package ru.mirea.kotiki.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.ChatMessage;

public interface PubSubService {
    Mono<Void> publish(ChatMessage message);
    Flux<ChatMessage> subscribe();
}
