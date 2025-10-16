# AuthService Microservice

This microservice is responsible for user authentication and authorization in the SkillSwap platform.

## Key Technologies

- **Spring Boot:** The core framework for building the application.
- **Spring Security:** For handling authentication and authorization.
- **Spring Data JPA:** For database interaction.
- **JWT:** For token-based authentication.

## Code Overview

- **`config`:** Contains configuration classes, such as `SecurityConfig` for setting up security rules and `OpenApiConfig` for API documentation.
- **`controller`:** The `AuthController` handles all authentication-related requests, such as login, registration, and token refreshing.
- **`exception`:** Defines custom exceptions for handling specific error scenarios, like `UserAlreadyExistsException` or `InvalidCredentialsException`.
- **`mapper`:** Contains `UserMapper` for converting between `User` entities and DTOs.
- **`model`:** Includes the data models, such as `User` entity and various DTOs for request and response objects.
- **`repository`:** Contains Spring Data JPA repositories for database operations on entities like `User`, `RefreshToken`, and `ConfirmationToken`.
- **`security`:** This package holds JWT-related utilities for token generation and validation, as well as user details services.
- **`service`:** The `AuthService` and `EmailService` contain the core business logic for user authentication, registration, and sending emails.
- **`validation`:** Implements custom validation logic, such as password matching and ensuring that at least one of a set of fields is not blank.
- **`AuthServiceApplication.java`:** The main entry point for the Spring Boot application.