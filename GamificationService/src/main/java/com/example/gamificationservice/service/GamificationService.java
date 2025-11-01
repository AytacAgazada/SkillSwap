package com.example.gamificationservice.service;

import com.example.gamificationservice.dto.AddXpDto;
import com.example.gamificationservice.dto.UserStatsDto;
import com.example.gamificationservice.entity.UserStats;
import com.example.gamificationservice.entity.XpTransaction;
import com.example.gamificationservice.event.ProblemSolvedEvent;
import com.example.gamificationservice.event.SwapCompletedEvent;
import com.example.gamificationservice.mapper.UserStatsMapper;
import com.example.gamificationservice.repository.UserStatsRepository;
import com.example.gamificationservice.repository.XpTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserStatsRepository userStatsRepository;
    private final XpTransactionRepository xpTransactionRepository;
    private final UserStatsMapper userStatsMapper;

    public UserStatsDto getUserStats(String userId) {
        UserStats userStats = userStatsRepository.findByUserId(userId).orElseGet(() -> createNewUserStats(userId));
        return userStatsMapper.toDto(userStats);
    }

    public void addXp(AddXpDto addXpDto) {
        UserStats userStats = userStatsRepository.findByUserId(addXpDto.getUserId()).orElseGet(() -> createNewUserStats(addXpDto.getUserId()));

        userStats.setXp(userStats.getXp() + addXpDto.getXp());
        checkForLevelUp(userStats);

        userStatsRepository.save(userStats);

        XpTransaction xpTransaction = new XpTransaction();
        xpTransaction.setUserId(addXpDto.getUserId());
        xpTransaction.setXpGained(addXpDto.getXp());
        xpTransaction.setEventType(addXpDto.getEventType());
        xpTransaction.setCreatedAt(LocalDateTime.now());
        xpTransactionRepository.save(xpTransaction);

        log.info("Added {} XP to user {} for event {}", addXpDto.getXp(), addXpDto.getUserId(), addXpDto.getEventType());
    }

    public UserStatsDto getBadges(String userId) {
        return getUserStats(userId);
    }

    @KafkaListener(topics = "problem-solved-topic", groupId = "gamification-group")
    public void consumeProblemSolvedEvent(ProblemSolvedEvent event) {
        log.info("Consumed ProblemSolvedEvent: {}", event);
        addXp(new AddXpDto(event.getSolvedByUserId(), 30, "ProblemSolved"));
    }

    @KafkaListener(topics = "swap-completed-topic", groupId = "gamification-group")
    public void consumeSwapCompletedEvent(SwapCompletedEvent event) {
        log.info("Consumed SwapCompletedEvent: {}", event);
        addXp(new AddXpDto(event.getUserId(), 50, "SwapCompleted"));
    }

    private UserStats createNewUserStats(String userId) {
        UserStats userStats = new UserStats();
        userStats.setUserId(userId);
        userStats.setXp(0);
        userStats.setLevel(1);
        userStats.setBadges(new ArrayList<>());
        return userStatsRepository.save(userStats);
    }

    private void checkForLevelUp(UserStats userStats) {
        int currentXp = userStats.getXp();
        int currentLevel = userStats.getLevel();

        int newLevel = (currentXp / 100) + 1;

        if (newLevel > currentLevel) {
            userStats.setLevel(newLevel);
            if (!userStats.getBadges().contains("Helper")) {
                userStats.getBadges().add("Helper");
            }
            log.info("User {} leveled up to level {}", userStats.getUserId(), newLevel);
        }
    }
}
