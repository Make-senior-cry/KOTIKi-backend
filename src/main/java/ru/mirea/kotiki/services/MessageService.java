package ru.mirea.kotiki.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirea.kotiki.domain.ChatMessage;
import ru.mirea.kotiki.repositories.ChatMessageRepository;
import ru.mirea.kotiki.repositories.UserRepository;

@Service
@Slf4j
public class MessageService {

    private ChatMessageRepository messageRepo;
    private UserRepository userRepository;

    public MessageService(ChatMessageRepository messageRepo, UserRepository userRepository) {
        this.messageRepo = messageRepo;
        this.userRepository = userRepository;
    }


    public Mono<ChatMessage> saveMsg(ChatMessage msg, String email){
        return userRepository.getIdByEmail(email)
                .map(msg::setSenderId)
                .flatMap(messageRepo::save);
    }

    public Flux<ChatMessage> receiveNewFromSender(Long senderId, String receiverEmail){
        return userRepository.getIdByEmail(receiverEmail)
                .flatMapMany(receiverId ->
                        messageRepo.getAllMessagesForSenderAndReceiver(senderId, receiverId))
                .flatMap(msg -> messageRepo.save(msg.setChecked(true)));
    }
}
