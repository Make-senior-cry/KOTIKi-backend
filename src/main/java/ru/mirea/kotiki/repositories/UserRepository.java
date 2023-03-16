package ru.mirea.kotiki.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByEmail(String email);

    Mono<User> findById(Long id);

    Mono<Boolean> existsByEmail(String email);

    @Query("SELECT COUNT(*) FROM user_user WHERE following_id = :id")
    Mono<Integer> getFollowerCountById(@Param("id") Long id);

    @Query("SELECT COUNT(*) FROM user_user WHERE follower_id = :id")
    Mono<Integer> getFollowingCountById(@Param("id") Long id);

}
