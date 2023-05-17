package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.Post;
import ru.mirea.kotiki.dto.*;
import ru.mirea.kotiki.repositories.PostRepository;
import ru.mirea.kotiki.repositories.UserRepository;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;

@Service
@Slf4j
public class PostService {

    @Value("${domain}")
    private String domain;

    @Value("${post.images.path}")
    private String path;

    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Autowired
    public PostService(PostRepository postRepo, UserRepository userRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    public void createPost(String text, Mono<FilePart> imageFile, String email) {
        userRepo.getIdByEmail(email).flatMap(id -> Mono.just(Post.builder()
                        .text(text)
                        .authorId(id)
                        .creationTimestamp(new Timestamp(new Date().getTime()))
                        .isBanned(false)
                        .build()))
                .flatMap(p -> imageFile.flatMap(i -> {
                    i.transferTo(Paths.get(path).resolve(i.filename())).subscribe();
                    return Mono.just(p.setImagePath("/static/images/post/upload/" + i.filename()));
                }).switchIfEmpty(Mono.just(p)))
                .flatMap(postRepo::save)
                .subscribe();
    }

    public Mono<UserPageDto> loadUserPage(String email, Long authorId, Integer limit, Integer skip) {
        var dto = new UserPageDto();
        return userRepo.getIdByEmail(email)
                .flatMap(id -> setPosts(id, authorId, limit, skip, dto))
                .flatMap(p -> setAuthor(authorId, dto))
                .flatMap(p -> {
                    p.setLimit(limit)
                            .setSkip(skip)
                            .setHasPrev(skip != 0);
                    return Mono.just(p);
                })
                .flatMap(p -> postRepo.countPostsByAuthorId(authorId)
                        .flatMap(c -> Mono.just(p.setHasNext(skip + limit < c))));
    }

    private Mono<UserPageDto> setPosts(Long userId, Long authorId, Integer limit, Integer skip, UserPageDto userPage) {
        return postRepo.getPostsByUserId(authorId, limit, skip)
                .map(p -> LightPostDto.builder()
                        .imageUrl(p.getImagePath())
                        .banned(p.getIsBanned())
                        .id(p.getId())
                        .text(p.getText())
                        .createdAt(p.getCreationTimestamp())
                        .build())
                .flatMap(p -> postRepo.countLikesByPostId(p.getId())
                        .flatMap(c -> Mono.just(p.setLikesCount(c)))
                )
                .flatMap(p -> postRepo.countReportsByPostId(p.getId())
                        .flatMap(c -> Mono.just(p.setReportsCount(c))))
                .flatMap(p -> postRepo.checkLikeOfPost(userId, p.getId())
                        .flatMap(b -> Mono.just(p.setLiked(b))))
                .collectList()
                .map(userPage::setPosts);
    }

    private Mono<UserPageDto> setAuthor(Long userId, UserPageDto userPage) {
        return userRepo.findById(userId)
                .map(UserDto::new)
                .flatMap(u -> setAdditionalInfo(Mono.just(u)))
                .map(userPage::setAuthor);
    }

    public Mono<LikeDto> likePost(String email, Long postId) {
        return userRepo.getIdByEmail(email)
                .flatMap(id ->
                        postRepo.existsLikeByPostIdAndUserId(postId, id)
                                .flatMap(exists -> {
                                    if (exists)
                                        return postRepo.deleteLikeByPostIdAndUserId(postId, id);
                                    else
                                        return postRepo.saveLike(postId, id);
                                })
                                .then(buildLikeDto(id, postId))
                );
    }

    private Mono<LikeDto> buildLikeDto(Long id, Long postId) {
        var likeDto = new LikeDto();
        return postRepo.countLikesByPostId(postId)
                .flatMap(c -> Mono.just(likeDto.setLikesCount(c)))
                .flatMap(dto -> postRepo.checkLikeOfPost(id, postId)
                        .flatMap(b -> Mono.just(dto.setLiked(b))));
    }

    public Mono<Void> banPost(Long postId) {
        return postRepo.banPostByPostId(postId);
    }

    public Mono<Boolean> reportPost(String email, Long postId) {
        return userRepo.getIdByEmail(email)
                .flatMap(ui -> postRepo.existsReportByPostIdAndUserId(postId, ui)
                        .flatMap(exists -> {
                            if (!exists) {
                                postRepo.saveReport(postId, ui).subscribe();
                                return Mono.just(true);
                            } else
                                return Mono.just(false);
                        }));
    }

    public Mono<FeedDto> getFollowingPosts(String email, Integer skip, Integer limit) {
        return postRepo.getFollowingPosts(email, skip, limit)
                .flatMap(this::setAuthor)
                .flatMap(p -> userRepo.getIdByEmail(email)
                        .flatMap(id -> postRepo.checkLikeOfPost(id, p.getId())
                                .flatMap(b -> Mono.just(p.setLiked(b)))))
                .collectList()
                .flatMap(l -> {
                    var dto = new FeedDto();
                    return postRepo.countFollowingPosts(email)
                            .flatMap(c -> Mono.just(dto.setHasNext(c - skip - limit > 0)))
                            .map(d -> d.setSkip(skip).setLimit(limit).setHasPrev(skip != 0).setPosts(l));
                });
    }

    public Mono<FeedDto> getNewPosts(String email, Integer skip, Integer limit) {
        return postRepo.getNewPosts(email, skip, limit)
                .flatMap(this::setAuthor)
                .flatMap(p -> userRepo.getIdByEmail(email)
                        .flatMap(id -> postRepo.checkLikeOfPost(id, p.getId()))
                        .flatMap(b -> Mono.just(p.setLiked(b))))
                .collectList()
                .flatMap(l -> {
                    FeedDto dto = new FeedDto();
                    return postRepo.countPosts().flatMap(c -> Mono.just(dto.setHasNext(c - skip - limit > 0)))
                            .map(d -> d.setSkip(skip).setLimit(limit).setHasPrev(skip != 0).setPosts(l));
                });
    }

    private Mono<PostDto> setAuthor(Post post) {
        return userRepo.findById(post.getAuthorId())
                .map(UserDto::new)
                .flatMap(u -> setAdditionalInfo(Mono.just(u)))
                .flatMap(u ->
                        Mono.just(PostDto.builder()
                                .author(u)
                                .createdAt(post.getCreationTimestamp())
                                .text(post.getText())
                                .id(post.getId())
                                .banned(post.getIsBanned())
                                .imageUrl(post.getImagePath())
                                .build())
                )
                .flatMap(p -> postRepo.countLikesByPostId(p.getId())
                        .flatMap(c -> Mono.just(p.setLikesCount(c))))
                .flatMap(p -> postRepo.countReportsByPostId(p.getId())
                        .flatMap(c -> Mono.just(p.setReportsCount(c))))
                .flatMap(p -> postRepo.checkLikeOfPost(post.getAuthorId(), p.getId())
                        .flatMap(b -> Mono.just(p.setLiked(b))));
    }

    private Mono<UserDto> setAdditionalInfo(Mono<UserDto> user) {
        return user
                .flatMap(u -> userRepo.getFollowingCountById(u.getId())
                        .map(u::setFollowingCount))
                .flatMap(u -> userRepo.getFollowersCountById(u.getId())
                        .map(u::setFollowersCount))
                .flatMap(u -> userRepo.getPostsCountById(u.getId())
                        .map(u::setPostsCount));
    }
}
