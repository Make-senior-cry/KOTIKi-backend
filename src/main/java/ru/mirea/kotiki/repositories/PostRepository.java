package ru.mirea.kotiki.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.Post;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
    @Query("SELECT * FROM post WHERE author_id = :userId AND is_banned != true ORDER BY id DESC LIMIT :limit OFFSET :skip")
    Flux<Post> getPostsByUserId(Long userId, Integer limit, Integer skip);

    @Query("SELECT COUNT(*) FROM post WHERE author_id = :userId AND is_banned != true")
    Mono<Integer> countPostsByAuthorId(Long userId);

    @Query("INSERT INTO post_like(post_id, user_id) VALUES(:postId, :userId)")
    Mono<Void> saveLike(Long postId, Long userId);

    @Query("SELECT COUNT(*) FROM post_like WHERE post_id = :postId")
    Mono<Integer> countLikesByPostId(Long postId);

    @Query("SELECT EXISTS(SELECT * FROM post_like WHERE post_id = :postId AND user_id = :userId)")
    Mono<Boolean> existsLikeByPostIdAndUserId(Long postId, Long userId);

    @Query("DELETE FROM post_like WHERE post_id = :postId AND user_id = :userId")
    Mono<Void> deleteLikeByPostIdAndUserId(Long postId, Long userId);

    @Query("UPDATE post SET is_banned = true WHERE id = :postId")
    Mono<Void> banPostByPostId(Long postId);

    @Query("INSERT INTO post_report(post_id, user_id) VALUES(:postId, :userId)")
    Mono<Void> saveReport(Long postId, Long userId);

    @Query("SELECT EXISTS(SELECT * FROM post_report WHERE post_id = :postId AND user_id = :userId)")
    Mono<Boolean> existsReportByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(*) FROM post_report WHERE post_id = :postId")
    Mono<Integer> countReportsByPostId(Long postId);

    @Query("SELECT * FROM post WHERE author_id != (SELECT id FROM usr WHERE email = :email) " +
            "ORDER BY creation_timestamp DESC LIMIT :limit OFFSET :skip")
    Flux<Post> getNewPosts(String email, Integer skip, Integer limit);

    @Query("SELECT COUNT(*) FROM post")
    Mono<Long> countPosts();

    @Query("SELECT * FROM post WHERE author_id IN (SELECT following_id FROM user_user WHERE follower_id = " +
            "(SELECT id FROM usr WHERE email = :email)) ORDER BY creation_timestamp DESC LIMIT :limit OFFSET :skip")
    Flux<Post> getFollowingPosts(String email, Integer skip, Integer limit);

    @Query("SELECT COUNT(*) FROM post WHERE author_id IN (SELECT following_id FROM user_user WHERE follower_id = " +
            "(SELECT id FROM usr WHERE email = :email))")
    Mono<Long> countFollowingPosts(String email);

    @Query("SELECT EXISTS(SELECT * FROM post_like WHERE user_id = :userId AND post_id = :postId)")
    Mono<Boolean> checkLikeOfPost(Long userId, Long postId);
}
