package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.repositories.UserRepository;

import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Value("${server.address}")
    private String serverAddress;

    @Value("${user.images.path}")
    private String path;

    private final UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }


    public Mono<UserDto> getUser(Long id){
        return userRepo.findById(id)
                .switchIfEmpty(Mono.error(new Exception()))
                .map(UserDto::new)
                .flatMap(dto -> userRepo.getFollowingCountById(id)
                        .flatMap(i -> Mono.just(dto.setFollowingCount(i))))
                .flatMap(dto -> userRepo.getFollowerCountById(id)
                        .flatMap(i -> Mono.just(dto.setFollowersCount(i))));
    }

    public Mono<UserDto> getUser(String email) {
        return userRepo.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception()))
                .map(UserDto::new)
                .flatMap(dto -> userRepo.getFollowingCountById(dto.getId())
                        .flatMap(i -> Mono.just(dto.setFollowingCount(i))))
                .flatMap(dto -> userRepo.getFollowerCountById(dto.getId())
                        .flatMap(i -> Mono.just(dto.setFollowersCount(i))));
    }


    public Mono<UserDto> updateUser(String email, String name, String description, Mono<FilePart> image) {
        log.info(email);
        return userRepo.findByEmail(email)
                .flatMap(u -> Mono.just(u.setName(name)))
                .flatMap(u -> Mono.just(u.setDescription(description)))
                .flatMap(u -> image.flatMap(fp -> {
                    fp.transferTo(Paths.get(path).resolve(fp.filename())).subscribe();
                    return Mono.just(u.setImagePath(serverAddress + "/static/images/user/upload/" + fp.filename()));
                }))
                .flatMap(userRepo::save)
                .flatMap(u -> getUser(u.getId()));

    }

    public Mono<List<UserDto>> searchUsers(String name, Integer skip, Integer limit) {
        return userRepo.searchUsersByName(name, skip, limit)
                .map(UserDto::new)
                .doOnNext(u -> userRepo.getFollowingCountById(u.getId())
                        .map(u::setFollowingCount).subscribe())
                .doOnNext(u -> userRepo.getFollowerCountById(u.getId())
                        .map(u::setFollowersCount).subscribe())
                .collectList();
    }

    public Mono<Boolean> getNext(String name, Integer skip, Integer limit) {
        return userRepo.countUsersByName(name).flatMap(c -> Mono.just( skip + limit < c));
    }
}
