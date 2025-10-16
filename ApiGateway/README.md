# ApiGateway Microservice

This microservice acts as an API Gateway for the SkillSwap platform. It is built using Spring Cloud Gateway and is responsible for routing requests to the appropriate downstream microservices.

## Key Technologies

- **Spring Cloud Gateway:** Provides a way to route requests to other services, and also to provide cross-cutting concerns like security, monitoring/metrics, and resiliency.
- **Spring Security:** Used to handle authentication and authorization.
- **JSON Web Tokens (JWT):** Used for securing the APIs.

## Code Overview

- **`ApiGatewayApplication.java`:** The main entry point for the Spring Boot application.
- **`application.properties`:** Contains the configuration for the application, including the JWT secret key. The routing rules are likely configured here or in a separate Java configuration file, and it probably uses a service discovery mechanism like Eureka to find the other services.