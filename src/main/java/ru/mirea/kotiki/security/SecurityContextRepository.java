package ru.mirea.kotiki.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final ReactiveAuthenticationManager authenticationManager;

    @Autowired
    public SecurityContextRepository(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new IllegalStateException("Save method not supported!");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String accessToken;

        List<HttpCookie> cookies = exchange.getRequest()
                .getCookies()
                .get("access-token");

        if (cookies != null) {
            accessToken = cookies.stream()
                    .filter(c -> c.getName().equals("access-token"))
                    .map(HttpCookie::getValue).findFirst().get();
        } else {
            return Mono.empty();
        }

        var auth = new UsernamePasswordAuthenticationToken(accessToken, accessToken);
        return authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
    }
}