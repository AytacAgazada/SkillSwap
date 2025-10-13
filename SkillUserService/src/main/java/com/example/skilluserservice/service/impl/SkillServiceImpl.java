package com.example.skilluserservice.service.impl;

import com.example.skilluserservice.dto.SkillCreateDto;
import com.example.skilluserservice.dto.SkillResponseDto;
import com.example.skilluserservice.dto.SkillUpdateDto;
import com.example.skilluserservice.entity.Skill;
import com.example.skilluserservice.exception.ResourceNotFoundException;
import com.example.skilluserservice.mapper.SkillMapper;
import com.example.skilluserservice.repository.SkillRepository;
import com.example.skilluserservice.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillResponseDto createSkill(SkillCreateDto skillCreateDto) {
        Skill skill = skillMapper.toEntity(skillCreateDto);
        return skillMapper.toDto(skillRepository.save(skill));
    }

    @Override
    public SkillResponseDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        return skillMapper.toDto(skill);
    }

    @Override
    public List<SkillResponseDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponseDto updateSkill(SkillUpdateDto skillUpdateDto) {
        Skill existingSkill = skillRepository.findById(skillUpdateDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillUpdateDto.getId()));
        skillMapper.updateEntityFromDto(skillUpdateDto, existingSkill);
        return skillMapper.toDto(skillRepository.save(existingSkill));
    }

    @Override
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found with id: " + id);
        }
        skillRepository.deleteById(id);
    }
}
