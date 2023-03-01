package ru.mirea.kotiki.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.mirea.kotiki.domain.ChatMessage;

public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {
}
