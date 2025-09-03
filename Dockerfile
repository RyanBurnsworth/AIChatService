FROM eclipse-temurin:17-jdk-jammy

ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080
