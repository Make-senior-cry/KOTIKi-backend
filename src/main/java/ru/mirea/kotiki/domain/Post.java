package ru.mirea.kotiki.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    private Long id;

    private String text;

    private String imagePath;

    private Timestamp creationTimestamp;

    private Integer reports;

    private Long authorId;

    public Post setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }
}
