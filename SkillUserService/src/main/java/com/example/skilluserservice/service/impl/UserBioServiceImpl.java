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
    private final AuthClient authClient; // Auth Service Feign client

    @Override
    @Transactional
    public UserBioResponseDTO createUserBio(UserBioCreateDto userBioCreateDto) {
        UUID authUserId = userBioCreateDto.getAuthUserId();

        // 1️⃣ AuthService-də istifadəçi varlığını yoxlayırıq
        Boolean exists = authClient.doesUserExist(authUserId);
        if (exists == null || !exists) {
            throw new InvalidInputException("Auth user with ID " + authUserId + " does not exist.");
        }

        // 2️⃣ Role yoxlaması (USER olmalıdır)
        String role = authClient.getUserRole(authUserId);
        if (!"USER".equalsIgnoreCase(role)) {
            throw new InvalidInputException("Auth user with ID " + authUserId + " is not a USER.");
        }

        // 3️⃣ UserBio artıq var mı yoxlayırıq
        if (userBioRepository.findByAuthUserId(authUserId).isPresent()) {
            throw new InvalidInputException("User Bio with authUserId " + authUserId + " already exists.");
        }

        UserBio userBio = userBioMapper.toEntity(userBioCreateDto);

        // 4️⃣ Skills map-ləmək
        Set<Skill> skills = mapSkills(userBioCreateDto.getSkillIds());
        userBio.setSkills(skills);

        // 5️⃣ Yaddaşa yazmaq
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
    public UserBioResponseDTO updateUserBio(UserBioUpdateDTO updateDTO) {
        UUID authUserId = updateDTO.getAuthUserId();

        UserBio existing = userBioRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Bio not found with id: " + updateDTO.getId()));

        // Əgər authUserId dəyişirsə, yoxlayırıq
        if (!existing.getAuthUserId().equals(authUserId)) {
            if (userBioRepository.findByAuthUserId(authUserId).isPresent()) {
                throw new InvalidInputException("User Bio with authUserId " + authUserId + " already exists.");
            }

            // AuthService-də yoxlayırıq
            Boolean exists = authClient.doesUserExist(authUserId);
            if (exists == null || !exists) {
                throw new InvalidInputException("Auth user with ID " + authUserId + " does not exist.");
            }

            String role = authClient.getUserRole(authUserId);
            if (!"USER".equalsIgnoreCase(role)) {
                throw new InvalidInputException("Auth user with ID " + authUserId + " is not a USER.");
            }
        }

        // Entity-ni DTO ilə yeniləyirik
        userBioMapper.updateEntityFromDto(updateDTO, existing);

        // Skills yeniləmə
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

    // --- Helper method ---
    private Set<Skill> mapSkills(Set<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return Collections.emptySet();

        Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillIds));
        if (skills.size() != skillIds.size()) {
            throw new InvalidInputException("One or more skill IDs are invalid.");
        }
        return skills;
    }
}
