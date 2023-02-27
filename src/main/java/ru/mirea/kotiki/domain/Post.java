package ru.mirea.kotiki.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Post {
    private long id;

    private String text;

    private String imagePath;

    private Timestamp creationTimestamp;

    private int reports;
}
