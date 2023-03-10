package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class Post {
    @Id
    private Long id;

    private String text;

    private String imagePath;

    private Timestamp creationTimestamp;

    private int reports;
}
