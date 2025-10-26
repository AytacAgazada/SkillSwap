FROM gradle:8.5.0-jdk17-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY gradle /app/gradle
COPY gradlew /app/
RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
