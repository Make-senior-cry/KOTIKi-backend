package ru.mirea.kotiki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.domain.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private String email;
    private UserRole role;
    private Integer followersCount;
    private Integer followingCount;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.imageUrl = user.getImagePath();
        this.description = user.getDescription();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
