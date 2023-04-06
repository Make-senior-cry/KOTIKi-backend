package ru.mirea.kotiki.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Mono<User> findById(Long id);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT COUNT(*) FROM user_user WHERE following_id = :id")
    Mono<Integer> getFollowersCountById(Long id);

    @Query("SELECT COUNT(*) FROM user_user WHERE follower_id = :id")
    Mono<Integer> getFollowingCountById(Long id);

    @Query("SELECT * FROM usr WHERE name LIKE CONCAT('%', :name, '%') LIMIT :limit OFFSET :skip")
    Flux<User> searchUsersByName(String name, Integer skip, Integer limit);

    Mono<Integer> countUsersByName(String name);

    @Query("SELECT id FROM usr WHERE email = :email")
    Mono<Long> getIdByEmail(String email);
}
