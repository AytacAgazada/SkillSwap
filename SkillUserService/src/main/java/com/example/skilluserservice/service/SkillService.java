package com.example.skilluserservice.service;

import com.example.skilluserservice.dto.SkillCreateDto;
import com.example.skilluserservice.dto.SkillResponseDto;
import com.example.skilluserservice.dto.SkillUpdateDto;

import java.util.List;

public interface SkillService {
    SkillResponseDto createSkill(SkillCreateDto skillCreateDto);
    SkillResponseDto getSkillById(Long id);
    List<SkillResponseDto> getAllSkills();
    SkillResponseDto updateSkill(SkillUpdateDto skillUpdateDto);
    void deleteSkill(Long id);
}