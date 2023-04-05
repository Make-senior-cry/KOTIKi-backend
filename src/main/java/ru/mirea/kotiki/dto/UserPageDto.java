package ru.mirea.kotiki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPageDto {
    private List<LightPostDto> posts;
    private UserDto author;
    private Boolean hasNext;
    private Boolean hasPrev;
    private Integer skip;
    private Integer limit;

    public UserPageDto setPosts(List<LightPostDto> posts) {
        this.posts = posts;
        return this;
    }

    public UserPageDto setAuthor(UserDto author) {
        this.author = author;
        return this;
    }

    public UserPageDto setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
        return this;
    }

    public UserPageDto setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
        return this;
    }

    public UserPageDto setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    public UserPageDto setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }
}
