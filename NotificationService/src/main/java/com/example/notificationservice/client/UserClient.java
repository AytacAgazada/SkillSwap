package com.example.notificationservice.client;

import com.example.notificationservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "skill-user-service", path = "/api/users")
public interface UserClient {

    @GetMapping("/{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);
}
