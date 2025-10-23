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

    /**
     * Cari autentifikasiya olunmuş istifadəçi üçün yeni bio yaradır.
     * Təhlükəsizlik: Auth ID birbaşa başlıqdan alınır.
     */
    @PostMapping
    public ResponseEntity<UserBioResponseDTO> createUserBio(
            // Başlığı qəbul edir: Kimin adından yaradılır.
            @RequestHeader("X-Auth-User-Id") UUID authUserId,
            @Valid @RequestBody UserBioCreateDto userBioCreateDto) {

        // Servisə həm başlıqdan gələn təsdiqlənmiş ID-ni, həm də DTO-nu ötürürük.
        UserBioResponseDTO createdUserBio = userBioService.createUserBio(authUserId, userBioCreateDto);
        return new ResponseEntity<>(createdUserBio, HttpStatus.CREATED);
    }

    /**
     * Daxili DB ID ilə bio gətirir (Məsələn, Admin və ya daxili zənglər).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserBioResponseDTO> getUserBioById(@PathVariable Long id) {
        UserBioResponseDTO userBioResponseDTO = userBioService.getUserBioById(id);
        return ResponseEntity.ok(userBioResponseDTO);
    }

    /**
     * Auth ID (UUID) ilə bio gətirir.
     */
    @GetMapping("/auth-user/{authUserId}")
    public ResponseEntity<UserBioResponseDTO> getUserBioByAuthUserId(@PathVariable UUID authUserId) {
        UserBioResponseDTO userBioResponseDTO = userBioService.getUserBioByAuthUserId(authUserId);
        return ResponseEntity.ok(userBioResponseDTO);
    }

    /**
     * Cari istifadəçinin bio-sunu əldə edir (Mənim profilim).
     */
    @GetMapping("/me")
    public ResponseEntity<UserBioResponseDTO> getMyBio(@RequestHeader("X-Auth-User-Id") UUID authUserId) {
        UserBioResponseDTO userBioResponseDTO = userBioService.getUserBioByAuthUserId(authUserId);
        return ResponseEntity.ok(userBioResponseDTO);
    }

    /**
     * Bütün bio-ları göstərir (Admin və ya Public axtarışlar üçün).
     */
    @GetMapping
    public ResponseEntity<List<UserBioResponseDTO>> getAllUserBios() {
        List<UserBioResponseDTO> userBios = userBioService.getAllUserBios();
        return ResponseEntity.ok(userBios);
    }

    /**
     * Cari istifadəçinin bio-sunu yeniləyir.
     * Təhlükəsizlik: Auth ID birbaşa başlıqdan alınır və Service-də yoxlanılır.
     */
    @PutMapping
    public ResponseEntity<UserBioResponseDTO> updateUserBio(
            // Başlığı qəbul edir: Yeniləməni kim edir.
            @RequestHeader("X-Auth-User-Id") UUID authUserId,
            @Valid @RequestBody UserBioUpdateDTO userBioUpdateDTO) {

        // Servisə təsdiqlənmiş ID-ni ötürürük ki, yalnız öz bio-sunu yeniləyə bilsin.
        UserBioResponseDTO updatedUserBio = userBioService.updateUserBio(authUserId, userBioUpdateDTO);
        return ResponseEntity.ok(updatedUserBio);
    }

    /**
     * Daxili DB ID ilə bio-nu silir (Admin funksiyası).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserBio(@PathVariable Long id) {
        userBioService.deleteUserBio(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cari istifadəçinin bio-sunu silir. (Son istifadəçi funksiyası)
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyBio(@RequestHeader("X-Auth-User-Id") UUID authUserId) {
        // Servisdə Auth ID ilə silinmə üçün yeni metod istifadə edilir.
        userBioService.deleteUserBioByAuthId(authUserId);
        return ResponseEntity.noContent().build();
    }
}