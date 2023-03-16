package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.repositories.UserRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class UserService {

    @Value("${server.address}")
    private String serverAddress;

    private final Path imagePath = Paths.get("D:\\KOTIKi-backend\\src\\main\\resources\\static\\images\\user\\upload\\");

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
        log.info(email);
        return userRepo.findByEmail(email)
                .flatMap(u -> Mono.just(u.setName(name)))
                .flatMap(u -> Mono.just(u.setDescription(description)))
                .flatMap(u -> image.flatMap(fp -> {
                    fp.transferTo(imagePath.resolve(fp.filename())).subscribe();
                    return Mono.just(u.setImagePath(serverAddress + "/static/images/user/upload/" + fp.filename()));
                }))
                .flatMap(userRepo::save)
                .flatMap(u -> getUser(u.getId()));

    }
}
