package com.example.skilluserservice.service.impl;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;
import com.example.skilluserservice.entity.Skill;
import com.example.skilluserservice.entity.UserBio;
import com.example.skilluserservice.exception.InvalidInputException;
import com.example.skilluserservice.exception.ResourceNotFoundException;
import com.example.skilluserservice.mapper.UserBioMapper;
import com.example.skilluserservice.repository.SkillRepository;
import com.example.skilluserservice.repository.UserBioRepository;
import com.example.skilluserservice.service.UserBioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBioServiceImpl implements UserBioService {

    private final UserBioRepository userBioRepository;
    private final SkillRepository skillRepository;
    private final UserBioMapper userBioMapper;

    @Override
    public UserBioResponseDTO createUserBio(UserBioCreateDto userBioCreateDto) {
        if (userBioRepository.findByAuthUserId(userBioCreateDto.getAuthUserId()).isPresent()) {
            throw new InvalidInputException("User Bio with authUserId " + userBioCreateDto.getAuthUserId() + " already exists.");
        }

        UserBio userBio = userBioMapper.toEntity(userBioCreateDto);

        Set<Skill> skills = new HashSet<>();
        if (userBioCreateDto.getSkillIds() != null && !userBioCreateDto.getSkillIds().isEmpty()) {
            skills = new HashSet<>(skillRepository.findAllById(userBioCreateDto.getSkillIds()));
            if (skills.size() != userBioCreateDto.getSkillIds().size()) {
                throw new InvalidInputException("One or more skill IDs are invalid.");
            }
        }
        userBio.setSkills(skills);

        return userBioMapper.toDto(userBioRepository.save(userBio));
    }

    @Override
    public UserBioResponseDTO getUserBioById(Long id) {
        UserBio userBio = userBioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + id));
        return userBioMapper.toDto(userBio);
    }

    @Override
    public UserBioResponseDTO getUserBioByAuthUserId(Long authUserId) {
        UserBio userBio = userBioRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with authUserId: " + authUserId));
        return userBioMapper.toDto(userBio);
    }

    @Override
    public List<UserBioResponseDTO> getAllUserBios() {
        return userBioRepository.findAll().stream()
                .map(userBioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserBioResponseDTO updateUserBio(UserBioUpdateDTO userBioUpdateDTO) {
        UserBio existingUserBio = userBioRepository.findById(userBioUpdateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + userBioUpdateDTO.getId()));

        // Check if authUserId is being changed to an existing one
        if (!existingUserBio.getAuthUserId().equals(userBioUpdateDTO.getAuthUserId())) {
            if (userBioRepository.findByAuthUserId(userBioUpdateDTO.getAuthUserId()).isPresent()) {
                throw new InvalidInputException("User Bio with authUserId " + userBioUpdateDTO.getAuthUserId() + " already exists.");
            }
        }

        userBioMapper.updateEntityFromDto(userBioUpdateDTO, existingUserBio);

        Set<Skill> skills = new HashSet<>();
        if (userBioUpdateDTO.getSkillIds() != null && !userBioUpdateDTO.getSkillIds().isEmpty()) {
            skills = new HashSet<>(skillRepository.findAllById(userBioUpdateDTO.getSkillIds()));
            if (skills.size() != userBioUpdateDTO.getSkillIds().size()) {
                throw new InvalidInputException("One or more skill IDs are invalid.");
            }
        }
        existingUserBio.setSkills(skills);

        return userBioMapper.toDto(userBioRepository.save(existingUserBio));
    }

    @Override
    public void deleteUserBio(Long id) {
        if (!userBioRepository.existsById(id)) {
            throw new ResourceNotFoundException("User Bio not found with id: " + id);
        }
        userBioRepository.deleteById(id);
    }
}
