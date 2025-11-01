package com.example.gamificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddXpDto {
    private String userId;
    private int xp;
    private String eventType;
}
