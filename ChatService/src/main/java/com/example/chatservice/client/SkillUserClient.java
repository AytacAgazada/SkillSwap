package com.example.chatservice.client;

import com.example.chatservice.dto.UserBioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "skill-user-service", url = "${skill-user-service.url}")
public interface SkillUserClient {

    @GetMapping("/api/user-bios/auth-user/{authUserId}")
    public ResponseEntity<UserBioResponseDTO> getUserBioByAuthUserId(@PathVariable UUID authUserId);
}
