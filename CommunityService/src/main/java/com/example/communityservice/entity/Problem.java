package com.example.communityservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String createdByUserId;
    private Long groupId;
    private boolean solved;
    private String solvedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime solvedAt;
}
