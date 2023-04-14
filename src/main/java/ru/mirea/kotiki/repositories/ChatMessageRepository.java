package ru.mirea.kotiki.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.mirea.kotiki.domain.ChatMessage;


@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

    @Query("SELECT * FROM chat_message WHERE sender_id = :senderId AND receiver_id = :receiverId AND checked = false")
    Flux<ChatMessage> getAllMessagesForSenderAndReceiver(long senderId, long receiverId);

    @Query("SELECT * FROM chat_message WHERE sender_id = :senderId")
    Flux<ChatMessage> getAllSent(long senderId);

//    @Query("SELECT * FROM chat_message WHERE checked = false AND chatroom_id = :chatRoomId")
//    Flux<ChatMessage> getAllUncheckedFromChatRoom(Long chatRoomId)

    @Query("SELECT * FROM chat_message WHERE checked = false")
    Flux<ChatMessage> findAllByCheckedFalse();


}
