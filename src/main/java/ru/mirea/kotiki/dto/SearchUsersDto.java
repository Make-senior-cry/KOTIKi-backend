package ru.mirea.kotiki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUsersDto {
    private List<UserDto> users;
    private Boolean hasPrev;
    private Boolean hasNext;
    private Integer skip;
    private Integer limit;
}
