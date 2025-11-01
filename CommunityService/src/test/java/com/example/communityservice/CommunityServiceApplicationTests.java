package com.example.communityservice;

import com.example.communityservice.dto.CreateGroupDto;
import com.example.communityservice.service.CommunityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CommunityServiceApplicationTests {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("community_db")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private CommunityService communityService;

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateGroup() {
        CreateGroupDto createGroupDto = new CreateGroupDto();
        createGroupDto.setName("Test Group");
        createGroupDto.setDescription("Test Description");
        createGroupDto.setCategory("Test Category");
        createGroupDto.setCreatedByUserId("test-user");

        var group = communityService.createGroup(createGroupDto);

        assertThat(group).isNotNull();
        assertThat(group.getName()).isEqualTo("Test Group");
    }

}