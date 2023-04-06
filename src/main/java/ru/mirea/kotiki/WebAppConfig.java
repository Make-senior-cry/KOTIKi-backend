package ru.mirea.kotiki;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

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
    public RouterFunction<ServerResponse> postImageRouter() {
        return RouterFunctions
                .resources("/static/images/post/upload/**", new ClassPathResource("static/images/post/upload/"));
    }
}
