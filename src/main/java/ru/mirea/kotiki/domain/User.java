package ru.mirea.kotiki.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class User {
    private long id;

    private String name;

    private String imagePath;

    private String description;

    private String email;

    private String passwordHash;

    private UserRole role;

    private Timestamp creationTimestamp;
}
