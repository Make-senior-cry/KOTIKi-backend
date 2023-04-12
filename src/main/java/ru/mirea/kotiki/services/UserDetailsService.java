package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.domain.UserRole;
import ru.mirea.kotiki.repositories.UserRepository;

import java.sql.Timestamp;
import java.util.Date;

@Service
@Slf4j
public class UserDetailsService implements ReactiveUserDetailsService {

    @Value("${domain}")
    private String domain;

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepo.findByEmail(email).cast(UserDetails.class);
    }

    public Mono<User> register(User user) {
        user.setRole(UserRole.ROLE_USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreationTimestamp(new Timestamp(new Date().getTime()));
        user.setImagePath(domain + "/static/images/user/default/default.jpg");
        return userRepo.existsByEmail(user.getEmail()).flatMap(exists -> {
            if (!exists) {
                return userRepo.save(user);
            }
            return Mono.empty();
        });
    }
}
