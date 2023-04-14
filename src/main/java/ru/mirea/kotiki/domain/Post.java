package ru.mirea.kotiki.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class Post {
    @Id
    private Long id;

    private String text;

    private String imagePath;

    private Timestamp creationTimestamp;

    private Boolean isBanned;

    private Long authorId;
}
