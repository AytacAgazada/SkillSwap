package com.example.gamificationservice.controller;

import com.example.gamificationservice.dto.AddXpDto;
import com.example.gamificationservice.dto.UserStatsDto;
import com.example.gamificationservice.service.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamification Service")
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user stats")
    public UserStatsDto getUserStats(@PathVariable String userId) {
        return gamificationService.getUserStats(userId);
    }

    @PostMapping("/add-xp")
    @Operation(summary = "Add XP to a user")
    public void addXp(@RequestBody AddXpDto addXpDto) {
        gamificationService.addXp(addXpDto);
    }

    @GetMapping("/badges/{userId}")
    @Operation(summary = "Get user badges")
    public UserStatsDto getBadges(@PathVariable String userId) {
        return gamificationService.getBadges(userId);
    }
}
