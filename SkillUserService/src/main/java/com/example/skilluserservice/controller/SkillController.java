package com.example.skilluserservice.controller;

import com.example.skilluserservice.dto.SkillCreateDto;
import com.example.skilluserservice.dto.SkillResponseDto;
import com.example.skilluserservice.dto.SkillUpdateDto;
import com.example.skilluserservice.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public ResponseEntity<SkillResponseDto> createSkill(@Valid @RequestBody SkillCreateDto skillCreateDto) {
        SkillResponseDto createdSkill = skillService.createSkill(skillCreateDto);
        return new ResponseEntity<>(createdSkill, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponseDto> getSkillById(@PathVariable Long id) {
        SkillResponseDto skillResponseDto = skillService.getSkillById(id);
        return ResponseEntity.ok(skillResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<SkillResponseDto>> getAllSkills() {
        List<SkillResponseDto> skills = skillService.getAllSkills();
        return ResponseEntity.ok(skills);
    }

    @PutMapping
    public ResponseEntity<SkillResponseDto> updateSkill(@Valid @RequestBody SkillUpdateDto skillUpdateDto) {
        SkillResponseDto updatedSkill = skillService.updateSkill(skillUpdateDto);
        return ResponseEntity.ok(updatedSkill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }
}
