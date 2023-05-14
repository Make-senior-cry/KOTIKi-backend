package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.repositories.UserRepository;

import java.util.List;

@Service
@Slf4j
public class UserService {

    @Value("${domain}")
    private String domain;

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
                .flatMap(u -> setRelationsCount(Mono.just(u)));
    }

    public Mono<UserDto> getUser(String email) {
        return userRepo.findByEmail(email)
                .switchIfEmpty(Mono.error(new Exception()))
                .map(UserDto::new)
                .flatMap(u -> setRelationsCount(Mono.just(u)));
    }

    /*
    public Mono<UserDto> updateUser(String email, String name, String description, Mono<FilePart> image) {
        log.info(email);
        return userRepo.findByEmail(email)
                .flatMap(u -> Mono.just(u.setName(name)))
                .flatMap(u -> Mono.just(u.setDescription(description)))
                .flatMap(u -> image.flatMap(fp -> {
                    log.debug(String.valueOf(Paths.get(path).resolve(fp.filename())));
                    return fp.transferTo(Paths.get(path).resolve(fp.filename()))
                            .then(Mono.just(u.setImagePath(domain
                                    + "/static/images/user/upload/"
                                    + fp.filename().replace("\n",""))));
                    //return Mono.just(u.setImagePath(domain + "/static/images/user/upload/" + fp.filename()));
                }))
                .flatMap(userRepo::save)
                .flatMap(u -> getUser(u.getId()));

    }
     */

    public Mono<UserDto> updateUser(String email, String name, String description) {
        log.info("updating user:" + email);
        return userRepo.findByEmail(email)
                .flatMap(u -> Mono.just(u.setName(name).setDescription(description)))
                .flatMap(userRepo::save)
                .flatMap(u -> getUser(u.getId()));
    }

    public Mono<List<UserDto>> searchUsers(String name, Integer skip, Integer limit) {
        return userRepo.searchUsersByName(name, skip, limit)
                .map(UserDto::new)
                .doOnNext(u -> userRepo.getFollowingCountById(u.getId())
                        .map(u::setFollowingCount).subscribe())
                .doOnNext(u -> userRepo.getFollowersCountById(u.getId())
                        .map(u::setFollowersCount).subscribe())
                .collectList();
    }

    public Mono<Boolean> getNext(String name, Integer skip, Integer limit) {
        return userRepo.countUsersByName(name).flatMap(c -> Mono.just( skip + limit < c));
    }

    public Mono<UserDto> followUser(String email, Long followingId) {
        return userRepo.getIdByEmail(email)
                .flatMap(id ->
                        userRepo.existsFollowByFollowerIdAndFollowingId(id, followingId)
                                .flatMap(exists -> {
                                    if (exists)
                                        return userRepo.deleteFollowByFollowerIdAndFollowingId(id, followingId);
                                    else
                                        return userRepo.saveFollow(id, followingId);
                                })
                                .then(userRepo.findById(id))
                )
                .map(UserDto::new)
                .flatMap(u -> setRelationsCount(Mono.just(u)));
    }

    private Mono<UserDto> setRelationsCount(Mono<UserDto> user) {
        return user
                .flatMap(u -> userRepo.getFollowingCountById(u.getId())
                        .map(u::setFollowingCount))
                .flatMap(u -> userRepo.getFollowersCountById(u.getId())
                        .map(u::setFollowersCount));
    }

    public Mono<Boolean> checkFollowing(String email, Long followingId) {
        return userRepo.getIdByEmail(email)
                        .flatMap(id -> userRepo.existsFollowByFollowerIdAndFollowingId(id, followingId));
    }
}
