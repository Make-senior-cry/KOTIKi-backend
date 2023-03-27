package ru.mirea.kotiki;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.mirea.kotiki.domain.ChatMessage;

@Configuration
public class WebAppConfig {
    @Bean
    public RouterFunction<ServerResponse> defaultUserImageRouter() {
        return RouterFunctions
                .resources("/static/images/user/default/**", new ClassPathResource("static/images/user/default/"));
    }
    @Bean
    public RouterFunction<ServerResponse> customUserImageRouter() {
        return RouterFunctions
                .resources("/static/images/user/upload/**", new ClassPathResource("static/images/user/upload/"));
    }

    @Bean
    public ReactiveRedisTemplate<String, ChatMessage> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ChatMessage> valueSerializer =
                new Jackson2JsonRedisSerializer<>(ChatMessage.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, ChatMessage> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, ChatMessage> context =
                builder.value(valueSerializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    ReactiveRedisMessageListenerContainer container(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisMessageListenerContainer(factory);
    }
}
