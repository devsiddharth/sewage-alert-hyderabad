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

# Copy source code required by Community Service
COPY community-service/src community-service/src

# Build Community Service and its required Maven dependencies
RUN mvn -pl community-service -am clean package -DskipTests


# ==============================
# Stage 2: Runtime
# ==============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated Community Service JAR
COPY --from=build /workspace/community-service/target/*.jar app.jar

# Community Service (optional GNews / Google Places / Overpass API keys via env vars)
EXPOSE 8084

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
