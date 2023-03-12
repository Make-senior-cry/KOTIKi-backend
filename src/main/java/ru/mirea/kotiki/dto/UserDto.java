package ru.mirea.kotiki.dto;

import lombok.Data;
import ru.mirea.kotiki.domain.UserRole;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private String email;
    private UserRole role;
    private Integer followersCount;
    private Integer followingCount;

}
