package com.example.skilluserservice.mapper;

import com.example.skilluserservice.dto.SkillCreateDto;
import com.example.skilluserservice.dto.SkillResponseDto;
import com.example.skilluserservice.dto.SkillUpdateDto;
import com.example.skilluserservice.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    SkillResponseDto toDto(Skill skill);

    @Mapping(target = "id", ignore = true)
    Skill toEntity(SkillCreateDto skillCreateDto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(SkillUpdateDto skillUpdateDto, @MappingTarget Skill skill);
}
