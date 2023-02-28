package ru.mirea.kotiki.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.mirea.kotiki.domain.Tag;

public interface TagRepository extends ReactiveCrudRepository<Tag, Long> {
}
