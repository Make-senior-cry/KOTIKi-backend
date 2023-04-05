package ru.mirea.kotiki.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.Post;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
    @Query("SELECT * FROM post WHERE author_id = :userId LIMIT :limit OFFSET :skip")
    Flux<Post> getPostsByUserId(Long userId, Integer limit, Integer skip);

    @Query("SELECT COUNT(*) FROM post WHERE author_id = :userId")
    Mono<Integer> countPostsByAuthorId(Long userId);
}
