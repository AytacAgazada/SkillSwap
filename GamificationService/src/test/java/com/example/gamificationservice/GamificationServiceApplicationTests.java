package com.example.gamificationservice;

import com.example.gamificationservice.dto.AddXpDto;
import com.example.gamificationservice.service.GamificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class GamificationServiceApplicationTests {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void mongodbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private GamificationService gamificationService;

    @Test
    void contextLoads() {
    }

    @Test
    void testAddXp() {
        AddXpDto addXpDto = new AddXpDto();
        addXpDto.setUserId("test-user");
        addXpDto.setXp(50);
        addXpDto.setEventType("Test Event");

        gamificationService.addXp(addXpDto);

        var userStats = gamificationService.getUserStats("test-user");

        assertThat(userStats).isNotNull();
        assertThat(userStats.getXp()).isEqualTo(50);
    }

}