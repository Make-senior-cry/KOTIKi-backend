package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.repositories.UserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class UserService {

    private final Path imagePath = Paths.get("./src/main/resources/images/user");

    private final UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }


    public Mono<UserDto> getUser(Long id){
        return userRepo.findById(id)
                .switchIfEmpty(Mono.error(new Exception()))
                .map(UserDto::new)
                .flatMap(dto -> userRepo.getFollowingCountById(id).flatMap(i -> Mono.just(dto.setFollowingCount(i))))
                .flatMap(dto -> userRepo.getFollowerCountById(id).flatMap(i -> Mono.just(dto.setFollowersCount(i))));

    }


    public Mono<UserDto> updateUser(String email, String name, String description, Mono<FilePart> image) {
        Mono<User> user = userRepo.findByEmail(email);
        AtomicReference<String> imageName = new AtomicReference<>();
        image.flatMap(fp -> {
            user.flatMap(u -> {
                u.setImagePath(imagePath.toString() + "/upload/" + fp.filename());
                return Mono.empty();
            });
            return fp.transferTo(imagePath.resolve("/upload/" + fp.filename()));
        }).switchIfEmpty(Mono.empty());
        if(name != null){
            user.flatMap(u -> {
                u.setName(name);
                return Mono.just(u);
            });
        }
        if(description != null){
            user.flatMap(u -> {
                u.setDescription(description);
                return Mono.just(u);
            });
        }
        return user.flatMap(userRepo::save).flatMap(u -> getUser(u.getId()));
    }
}
