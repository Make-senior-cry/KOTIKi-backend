package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class ChatRoom {
    @Id
    private Long id;

    private User firstUser;

    private User secondUser;
}
