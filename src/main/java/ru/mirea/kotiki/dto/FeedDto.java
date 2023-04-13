package ru.mirea.kotiki.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FeedDto {
    List<PostDto> posts;
    private Boolean hasNext;
    private Boolean hasPrev;
    private Integer skip;
    private Integer limit;
}
