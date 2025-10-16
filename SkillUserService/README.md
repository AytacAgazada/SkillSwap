# SkillUserService Microservice

This microservice manages user skills and biographical information for the SkillSwap platform.

## Key Technologies

- **Spring Boot:** The core framework for building the application.
- **Spring Data JPA:** For database interaction.

## Code Overview

- **`config`:** Contains configuration classes, such as `OpenApiConfig` for API documentation.
- **`controller`:** The `SkillController` and `UserBioController` handle all requests related to managing user skills and bios.
- **`dto`:** Contains Data Transfer Objects (DTOs) for creating, updating, and retrieving skill and user bio information.
- **`exception`:** Defines custom exceptions for handling specific error scenarios, like `ResourceNotFoundException`.
- **`entity`:** Includes the data models, such as the `Skill` and `UserBio` entities.
- **`mapper`:** Contains mappers for converting between entities and DTOs.
- **`repository`:** Contains Spring Data JPA repositories for database operations on the entities.
- **`service`:** The services contain the core business logic for managing user skills and bios.
- **`SkillUserServiceApplication.java`:** The main entry point for the Spring Boot application.