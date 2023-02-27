package ru.mirea.kotiki.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatMessage {
    private long id;

    private String text;

    private ChatRoom chatRoom;

    private Timestamp sendTimestamp;

    private User user;
}
