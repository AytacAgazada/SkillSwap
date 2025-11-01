package com.example.gamificationservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "user_stats")
public class UserStats {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private int xp;
    private int level;
    private List<String> badges;
}
