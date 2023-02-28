package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class User {
    @Id
    private Long id;

    private String name;

    private String imagePath;

    private String description;

    private String email;

    private String passwordHash;

    private UserRole role;

    private Timestamp creationTimestamp;
}
