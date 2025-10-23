package com.example.skillswapservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AcceptSwapRequest {

    @NotNull
    @Future
    private LocalDateTime meetingDateTime;
}
