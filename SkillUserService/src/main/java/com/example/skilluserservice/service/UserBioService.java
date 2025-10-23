package com.example.skilluserservice.service;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface UserBioService {

    UserBioResponseDTO createUserBio(UUID authUserId, UserBioCreateDto userBioCreateDto); // 👈 Düzəliş

    UserBioResponseDTO getUserBioById(Long id);

    UserBioResponseDTO getUserBioByAuthUserId(UUID authUserId);

    List<UserBioResponseDTO> getAllUserBios();

    UserBioResponseDTO updateUserBio(UUID authUserId, UserBioUpdateDTO userBioUpdateDTO);

    void deleteUserBio(Long id);

    void deleteUserBioByAuthId(UUID authUserId);
}