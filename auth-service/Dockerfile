# ==============================
# Stage 1: Build
# ==============================
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace

# Copy root Maven configuration
COPY pom.xml .

# Copy module POM files
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY complaint-service/pom.xml complaint-service/pom.xml
COPY community-service/pom.xml community-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

# Copy source code required by Auth Service
COPY auth-service/src auth-service/src

# Auth Service has a compile dependency on user-service
COPY user-service/src user-service/src

# Build Auth Service and its required Maven dependencies
RUN mvn -pl auth-service -am clean package -DskipTests


# ==============================
# Stage 2: Runtime
# ==============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated Auth Service JAR
COPY --from=build /workspace/auth-service/target/*.jar app.jar

# Auth Service
EXPOSE 8081

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]