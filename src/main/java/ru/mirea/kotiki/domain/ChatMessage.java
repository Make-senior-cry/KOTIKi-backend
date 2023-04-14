package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.Date;

@Table
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ChatMessage {
    @Id
    private Long id;

    private String text;

    //TODO require and connect with rabbit
//    @JsonProperty(required = false)
//    private ChatRoom chatRoom;

    private Timestamp sendTimestamp = new Timestamp(new Date().getTime());

    private Long senderId;

    private Long receiverId;

    private boolean checked = false;
}
