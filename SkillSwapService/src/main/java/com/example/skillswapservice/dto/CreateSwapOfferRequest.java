package com.example.skillswapservice.dto;

import com.example.skillswapservice.entity.SwapOfferEntity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSwapOfferRequest {

    @NotBlank
    private String skillOffered;

    @NotBlank
    private String skillRequested;

    @NotNull
    private SwapOfferEntity.MeetingType meetingType;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "-90.0", message = "Latitude -90.0-dan kiçik ola bilməz.")
    @DecimalMax(value = "90.0", message = "Latitude 90.0-dan böyük ola bilməz.")
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitude -180.0-dan kiçik ola bilməz.")
    @DecimalMax(value = "180.0", message = "Longitude 180.0-dan böyük ola bilməz.")
    private Double longitude;
}