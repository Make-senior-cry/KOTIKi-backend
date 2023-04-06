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

    @Query("INSERT INTO post_like(post_id, user_id) SELECT :postId, :userId " +
            "WHERE NOT EXISTS (SELECT * FROM post_like WHERE post_id = :postId AND user_id = :userId)")
    Mono<Integer> saveLike(Long postId, Long userId);

    @Query("SELECT COUNT(*) FROM post_like WHERE post_id = :postId")
    Mono<Integer> countLikesByPostId(Long postId);
}
