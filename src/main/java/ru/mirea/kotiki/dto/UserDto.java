package ru.mirea.kotiki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mirea.kotiki.domain.User;
import ru.mirea.kotiki.domain.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private String email;
    private UserRole role;
    private Integer followersCount;
    private Integer followingCount;


    public UserDto(User user, Integer followersCount, Integer followingCount) {
        this.id = user.getId();
        this.name = user.getName();
        this.imageUrl = user.getImagePath();
        this.description = user.getDescription();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.followingCount = followingCount;
        this.followersCount = followersCount;
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.imageUrl = user.getImagePath();
        this.description = user.getDescription();
        this.email = user.getEmail();
        this.role = user.getRole();

    }

    public UserDto setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public UserDto setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
        return this;
    }
}
