package ru.mirea.kotiki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserPageDto {
    private List<LightPostDto> posts;
    private UserDto author;
    private Boolean hasNext;
    private Boolean hasPrev;
    private Integer skip;
    private Integer limit;
}
