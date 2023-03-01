package ru.mirea.kotiki.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.mirea.kotiki.domain.Post;

public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
}
