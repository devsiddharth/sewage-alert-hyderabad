# ==============================
# Stage 1: Build
# ==============================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace

# Copy root Maven configuration
COPY pom.xml .

# Copy module POM files — the root reactor declares all modules,
# so every module POM must be present for the build to resolve.
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY complaint-service/pom.xml complaint-service/pom.xml
COPY community-service/pom.xml community-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

# Copy source code required by Notification Service
COPY notification-service/src notification-service/src

# Build Notification Service and its required Maven dependencies.
# Tests are skipped because the RabbitMQ integration test needs a Docker daemon.
RUN mvn -pl notification-service -am clean package -DskipTests


# ==============================
# Stage 2: Runtime
# ==============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated Notification Service JAR
COPY --from=build /workspace/notification-service/target/*.jar app.jar

# Notification Service (needs RabbitMQ + EmailJS env vars at runtime)
EXPOSE 8085

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
