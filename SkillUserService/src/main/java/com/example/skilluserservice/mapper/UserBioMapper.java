package com.example.skilluserservice.mapper;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;
import com.example.skilluserservice.entity.UserBio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SkillMapper.class})
public interface UserBioMapper {

    UserBioResponseDTO toDto(UserBio userBio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skills", ignore = true)
    UserBio toEntity(UserBioCreateDto userBioCreateDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateEntityFromDto(UserBioUpdateDTO userBioUpdateDTO, @MappingTarget UserBio userBio);
}
