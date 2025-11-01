package com.example.communityservice.mapper;

import com.example.communityservice.dto.CreateProblemDto;
import com.example.communityservice.dto.ProblemDto;
import com.example.communityservice.entity.Problem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProblemMapper {
    ProblemDto toDto(Problem problem);
    Problem toEntity(CreateProblemDto createProblemDto);
}
