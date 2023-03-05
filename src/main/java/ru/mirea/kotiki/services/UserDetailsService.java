package ru.mirea.kotiki.services;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.repositories.UserRepository;

@Service
public class UserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepo;

    public UserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepo.findByEmail(email).cast(UserDetails.class);
    }
}
