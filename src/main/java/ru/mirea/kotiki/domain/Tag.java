package ru.mirea.kotiki.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class Tag {
    @Id
    private Long id;

    private String name;
}
