package com.example.skilluserservice.controller;

import com.example.skilluserservice.dto.UserBioCreateDto;
import com.example.skilluserservice.dto.UserBioResponseDTO;
import com.example.skilluserservice.dto.UserBioUpdateDTO;
import com.example.skilluserservice.service.UserBioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-bios")
@RequiredArgsConstructor
public class UserBioController {

    private final UserBioService userBioService;

    @PostMapping
    public ResponseEntity<UserBioResponseDTO> createUserBio(@Valid @RequestBody UserBioCreateDto userBioCreateDto) {
        UserBioResponseDTO createdUserBio = userBioService.createUserBio(userBioCreateDto);
        return new ResponseEntity<>(createdUserBio, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserBioResponseDTO> getUserBioById(@PathVariable Long id) {
        UserBioResponseDTO userBioResponseDTO = userBioService.getUserBioById(id);
        return ResponseEntity.ok(userBioResponseDTO);
    }

    @GetMapping("/auth-user/{authUserId}")
    public ResponseEntity<UserBioResponseDTO> getUserBioByAuthUserId(@PathVariable UUID authUserId) {
        UserBioResponseDTO userBioResponseDTO = userBioService.getUserBioByAuthUserId(authUserId);
        return ResponseEntity.ok(userBioResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<UserBioResponseDTO>> getAllUserBios() {
        List<UserBioResponseDTO> userBios = userBioService.getAllUserBios();
        return ResponseEntity.ok(userBios);
    }

    @PutMapping
    public ResponseEntity<UserBioResponseDTO> updateUserBio(@Valid @RequestBody UserBioUpdateDTO userBioUpdateDTO) {
        UserBioResponseDTO updatedUserBio = userBioService.updateUserBio(userBioUpdateDTO);
        return ResponseEntity.ok(updatedUserBio);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserBio(@PathVariable Long id) {
        userBioService.deleteUserBio(id);
        return ResponseEntity.noContent().build();
    }
}
