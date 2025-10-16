# EurekaService Microservice

This microservice is the service discovery server for the SkillSwap platform. It is built using Netflix Eureka.

## Key Technologies

- **Spring Cloud Netflix Eureka Server:** Provides a service registry for all other microservices to register with and discover each other.

## Code Overview

- **`EurekaServiceApplication.java`:** The main entry point for the Spring Boot application. The `@EnableEurekaServer` annotation is used to enable the Eureka server functionality.
- **`application.properties`:** Contains the configuration for the Eureka server, such as the port it runs on and the configuration to prevent it from registering itself as a client.