package com.example.zandobackend.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Size {
    private Integer id;
    private UUID uuid = UUID.randomUUID();
    private String name;

    public Size(String name) {
        this.name = name;
    }
}