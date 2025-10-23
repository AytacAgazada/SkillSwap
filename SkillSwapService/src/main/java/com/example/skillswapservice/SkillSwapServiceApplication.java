package com.example.skillswapservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SkillSwapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillSwapServiceApplication.class, args);
    }

}
