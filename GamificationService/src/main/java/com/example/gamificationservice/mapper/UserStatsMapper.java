package com.example.gamificationservice.mapper;

import com.example.gamificationservice.dto.UserStatsDto;
import com.example.gamificationservice.entity.UserStats;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStatsMapper {
    UserStatsDto toDto(UserStats userStats);
}
