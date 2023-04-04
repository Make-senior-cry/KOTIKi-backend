package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.Post;
import ru.mirea.kotiki.repositories.PostRepository;
import ru.mirea.kotiki.repositories.UserRepository;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;

@Service
@Slf4j
public class PostService {

    @Value("${server.address}")
    private String serverAddress;

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
                        log.info("trans");
                        return Mono.just(p.setImagePath(serverAddress + "/static/images/post/upload/" + i.filename()));
                    }).switchIfEmpty(Mono.just(p)))
                    .flatMap(postRepo::save)
                    .subscribe();
    }
}
