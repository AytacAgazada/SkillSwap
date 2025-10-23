package com.example.skillswapservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class SwapOfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    private String skillOffered;
    private String skillRequested;

    @Enumerated(EnumType.STRING)
    private MeetingType meetingType;

    private String description;
    private boolean isActive = true;

    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MeetingType {
        PHYSICAL, ONLINE, BOTH
    }
}