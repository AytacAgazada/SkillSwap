package com.example.communityservice.mapper;

import com.example.communityservice.dto.CreateGroupDto;
import com.example.communityservice.dto.GroupDto;
import com.example.communityservice.entity.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupDto toDto(Group group);
    Group toEntity(CreateGroupDto createGroupDto);
}
