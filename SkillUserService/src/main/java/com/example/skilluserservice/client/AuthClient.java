package com.example.skilluserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface AuthClient {

    @GetMapping("/api/auth/{authUserId}/exists")
    Boolean doesUserExist(@PathVariable("authUserId") UUID authUserId);

    @GetMapping("/api/auth/{authUserId}/role")
    String getUserRole(@PathVariable("authUserId") UUID authUserId);

}
