package ru.mirea.kotiki.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.mirea.kotiki.domain.ChatRoom;

public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {
}
