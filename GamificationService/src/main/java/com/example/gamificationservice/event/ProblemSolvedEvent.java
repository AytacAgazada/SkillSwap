package com.example.gamificationservice.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemSolvedEvent {
    private Long problemId;
    private String solvedByUserId;
    private LocalDateTime solvedAt;
}
