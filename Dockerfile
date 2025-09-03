FROM eclipse-temurin:17-jdk-jammy

ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

# Copy the built jar into the container
COPY target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]
