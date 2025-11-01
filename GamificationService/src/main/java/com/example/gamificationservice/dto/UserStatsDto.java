package com.example.gamificationservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserStatsDto {
    private String userId;
    private int xp;
    private int level;
    private List<String> badges;
}
