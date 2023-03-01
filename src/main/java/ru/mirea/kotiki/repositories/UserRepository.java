package ru.mirea.kotiki.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    public Mono<User> findByEmail(String email);
}
