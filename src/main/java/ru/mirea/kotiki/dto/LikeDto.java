package ru.mirea.kotiki.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LikeDto {
    private Integer likesCount;
    private Boolean isLiked;
}
