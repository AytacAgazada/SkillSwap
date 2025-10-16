# 1️⃣ Build mərhələsi (Maven ilə)
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# 2️⃣ Run mərhələsi (lightweight image)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 1113
ENTRYPOINT ["java", "-jar", "app.jar"]
