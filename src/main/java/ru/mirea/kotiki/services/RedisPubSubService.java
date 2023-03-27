package ru.mirea.kotiki.services;

import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.ChatMessage;

import java.util.Collections;

@Service
public class RedisPubSubService implements PubSubService {

    private final ReactiveRedisTemplate<String, ChatMessage> reactiveTemplate;
    private final ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    private final ChannelTopic channelTopic = new ChannelTopic("broadcast"); // channel used to send and recieve messages

    public RedisPubSubService(ReactiveRedisTemplate<String, ChatMessage> reactiveTemplate,
                              ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer) {
        this.reactiveMsgListenerContainer = reactiveMsgListenerContainer;
        this.reactiveTemplate = reactiveTemplate;
    }

    @Override
    public Mono<Void> publish(ChatMessage message) {
        return this.reactiveTemplate
                .convertAndSend(channelTopic.getTopic(), message)
                .then(Mono.empty());
    }

    @Override
    public Flux<ChatMessage> subscribe() {
        return reactiveMsgListenerContainer
                .receive(Collections.singletonList(channelTopic),
                        reactiveTemplate.getSerializationContext().getKeySerializationPair(),
                        reactiveTemplate.getSerializationContext().getValueSerializationPair())
                .map(ReactiveSubscription.Message::getMessage);
    }
}

