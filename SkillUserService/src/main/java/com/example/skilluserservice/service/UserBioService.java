package com.example.skilluserservice.service;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;

import java.util.List;

public interface UserBioService {
    UserBioResponseDTO createUserBio(UserBioCreateDto userBioCreateDto);
    UserBioResponseDTO getUserBioById(Long id);
    UserBioResponseDTO getUserBioByAuthUserId(Long authUserId);
    List<UserBioResponseDTO> getAllUserBios();
    UserBioResponseDTO updateUserBio(UserBioUpdateDTO userBioUpdateDTO);
    void deleteUserBio(Long id);
}