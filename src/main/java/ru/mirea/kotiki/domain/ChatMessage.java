package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@NoArgsConstructor
@Data
public class ChatMessage {
    @Id
    private Long id;

    private String text;

    private ChatRoom chatRoom;

    private Timestamp sendTimestamp;

    private User user;
}
