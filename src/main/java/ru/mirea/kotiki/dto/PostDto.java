package ru.mirea.kotiki.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
public class PostDto {
    private Long id;
    private String text;
    private String imageUrl;
    private Date createdAt;
    private Boolean banned;
    private Integer likesCount;
    private Integer reportsCount;
    private UserDto author;
}
