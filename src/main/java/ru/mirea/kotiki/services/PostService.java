package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.Post;
import ru.mirea.kotiki.dto.LightPostDto;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.dto.UserPageDto;
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
                            .reports(0)
                            .creationTimestamp(new Timestamp(new Date().getTime()))
                            .isBanned(false)
                            .build()))
                    .flatMap(p -> imageFile.flatMap(i -> {
                        i.transferTo(Paths.get(path).resolve(i.filename())).subscribe();
                        return Mono.just(p.setImagePath(domain + "/static/images/post/upload/" + i.filename()));
                    }).switchIfEmpty(Mono.just(p)))
                    .flatMap(postRepo::save)
                    .subscribe();
    }

    public Mono<UserPageDto> loadUserPage(Long userId, Integer limit, Integer skip) {
        UserPageDto dto = new UserPageDto();
        return setPosts(userId, limit, skip, dto)
                .flatMap(p -> setAuthor(userId, dto))
                .flatMap(p -> {
                    p.setLimit(limit)
                            .setSkip(skip)
                            .setHasPrev(skip != 0);
                    return Mono.just(p);
                })
                .flatMap(p -> postRepo.countPostsByAuthorId(userId)
                        .flatMap(c -> Mono.just(p.setHasNext(skip + limit < c)))
                );
    }

    private Mono<UserPageDto> setPosts(Long userId, Integer limit, Integer skip, UserPageDto userPage) {
        return postRepo.getPostsByUserId(userId, limit, skip)
                .map(p -> LightPostDto.builder()
                        .imageUrl(p.getImagePath())
                        .banned(p.getIsBanned())
                        .id(p.getId())
                        .text(p.getText())
                        .createdAt(p.getCreationTimestamp())
                        .build())
                .collectList()
                .map(userPage::setPosts);
    }

    private Mono<UserPageDto> setAuthor(Long userId, UserPageDto userPage) {
        return userRepo.findById(userId)
                .map(UserDto::new)
                .doOnNext(u -> userRepo.getFollowingCountById(userId)
                        .map(u::setFollowingCount).subscribe())
                .doOnNext(u -> userRepo.getFollowersCountById(userId)
                        .map(u::setFollowersCount).subscribe())
                .map(userPage::setAuthor);
    }
}
