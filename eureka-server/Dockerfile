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

# Copy source code required by Eureka Server
COPY eureka-server/src eureka-server/src

# Build Eureka Server and its required Maven dependencies
RUN mvn -pl eureka-server -am clean package -DskipTests


# ==============================
# Stage 2: Runtime
# ==============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated Eureka Server JAR
COPY --from=build /workspace/eureka-server/target/*.jar app.jar

# Eureka Server (service discovery)
EXPOSE 8761

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
