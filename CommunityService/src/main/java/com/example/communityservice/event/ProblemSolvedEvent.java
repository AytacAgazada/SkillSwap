package com.example.communityservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSolvedEvent {
    private Long problemId;
    private String solvedByUserId;
    private LocalDateTime solvedAt;
}
