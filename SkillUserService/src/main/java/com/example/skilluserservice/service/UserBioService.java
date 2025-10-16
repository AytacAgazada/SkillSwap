package com.example.skilluserservice.service;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface UserBioService {

    UserBioResponseDTO createUserBio(UserBioCreateDto userBioCreateDto);

    UserBioResponseDTO getUserBioById(Long id);

    UserBioResponseDTO getUserBioByAuthUserId(UUID authUserId);

    List<UserBioResponseDTO> getAllUserBios();

    UserBioResponseDTO updateUserBio(UserBioUpdateDTO userBioUpdateDTO);

    void deleteUserBio(Long id);
}
