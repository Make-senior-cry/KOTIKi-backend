package ru.mirea.kotiki.domain;

import lombok.Data;

@Data
public class ChatRoom {
    private long id;

    private User firstUser;

    private User secondUser;
}
