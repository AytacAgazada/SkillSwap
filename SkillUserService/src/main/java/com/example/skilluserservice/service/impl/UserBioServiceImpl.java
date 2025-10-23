package com.example.skilluserservice.service.impl;

import com.example.skilluserservice.client.AuthClient;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBioServiceImpl implements UserBioService {

    private final UserBioRepository userBioRepository;
    private final SkillRepository skillRepository;
    private final UserBioMapper userBioMapper;
    private final AuthClient authClient;

    @Override
    @Transactional
    public UserBioResponseDTO createUserBio(UUID authUserId, UserBioCreateDto userBioCreateDto) {
        if (userBioRepository.findByAuthUserId(authUserId).isPresent()) {
            throw new InvalidInputException("User Bio with authUserId " + authUserId + " already exists. Only one bio is allowed.");
        }

        Boolean exists = authClient.doesUserExist(authUserId);
        if (exists == null || !exists) {
            throw new ResourceNotFoundException("Auth user with ID " + authUserId + " not found.");
        }
        String role = authClient.getUserRole(authUserId);
        if (!"USER".equalsIgnoreCase(role)) {
            throw new InvalidInputException("Auth user with ID " + authUserId + " is not authorized.");
        }

        UserBio userBio = userBioMapper.toEntity(userBioCreateDto);
        userBio.setAuthUserId(authUserId);

        Set<Skill> skills = mapSkills(userBioCreateDto.getSkillIds());
        userBio.setSkills(skills);

        UserBio saved = userBioRepository.save(userBio);
        log.info("Created UserBio for authUserId={}", authUserId);

        return userBioMapper.toDto(saved);
    }

    @Override
    public UserBioResponseDTO getUserBioById(Long id) {
        UserBio userBio = userBioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + id));
        return userBioMapper.toDto(userBio);
    }

    @Override
    public UserBioResponseDTO getUserBioByAuthUserId(UUID authUserId) {
        return userBioRepository.findByAuthUserId(authUserId)
                .map(userBioMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with authUserId: " + authUserId));
    }

    @Override
    public List<UserBioResponseDTO> getAllUserBios() {
        return userBioRepository.findAll().stream()
                .map(userBioMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserBioResponseDTO updateUserBio(UUID authUserId, UserBioUpdateDTO updateDTO) {
        UserBio existing = userBioRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + updateDTO.getId()));

        if (!existing.getAuthUserId().equals(authUserId)) {
            log.error("Access Denied: User {} attempted to update UserBio id={}", authUserId, existing.getId());
            throw new InvalidInputException("You are not authorized to update this user bio.");
        }

        userBioMapper.updateEntityFromDto(updateDTO, existing);

        Set<Skill> skills = mapSkills(updateDTO.getSkillIds());
        existing.setSkills(skills);

        UserBio saved = userBioRepository.save(existing);
        log.info("Updated UserBio id={} for authUserId={}", saved.getId(), authUserId);

        return userBioMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteUserBio(Long id) {
        UserBio existing = userBioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + id));

        userBioRepository.delete(existing);
        log.info("Deleted UserBio id={} for authUserId={}", existing.getId(), existing.getAuthUserId());
    }

    @Override
    @Transactional
    public void deleteUserBioByAuthId(UUID authUserId) {
        UserBio existing = userBioRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with authUserId: " + authUserId));

        userBioRepository.delete(existing);
        log.info("Deleted UserBio id={} by Auth ID={}", existing.getId(), existing.getAuthUserId());
    }

    private Set<Skill> mapSkills(Set<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return Collections.emptySet();

        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillIds));
        if (skills.size() != skillIds.size()) {
            throw new InvalidInputException("One or more skill IDs are invalid.");
        }
        return skills;
    }
}