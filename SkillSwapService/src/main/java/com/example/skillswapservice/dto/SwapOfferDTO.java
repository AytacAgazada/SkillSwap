package com.example.skillswapservice.dto;

import java.util.UUID;

import com.example.skillswapservice.entity.SwapOfferEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class SwapOfferDTO {
    Long id;
    UUID userId;
    String skillOffered;
    String skillRequested;
    SwapOfferEntity.MeetingType meetingType;
    String description;
    Double latitude;
    Double longitude;
}