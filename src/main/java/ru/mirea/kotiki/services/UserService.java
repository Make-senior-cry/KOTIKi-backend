package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.dto.UserDto;
import ru.mirea.kotiki.repositories.UserRepository;

@Service
@Slf4j
public class UserService {

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


}
