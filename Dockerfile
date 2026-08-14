# ============================================================
# Sewage Alert Hyderabad — Root Dockerfile
#
# Builds the entire Maven multi-module reactor in one image,
# then lets you pick which service to run at container start:
#
#   docker build -t sewage-alert-hyderabad .
#
#   docker run -e SERVICE_NAME=eureka-server      -p 8761:8761 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=api-gateway        -p 8080:8080 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=auth-service       -p 8081:8081 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=user-service       -p 8082:8082 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=complaint-service  -p 8083:8083 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=community-service  -p 8084:8084 sewage-alert-hyderabad
#   docker run -e SERVICE_NAME=notification-service -p 8085:8085 sewage-alert-hyderabad
#
# The SERVICE_NAME env var must match the module/artifact name.
# ============================================================

# ==============================
# Stage 1: Build all services
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

# Copy all service sources
COPY eureka-server/src eureka-server/src
COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY user-service/src user-service/src
COPY complaint-service/src complaint-service/src
COPY community-service/src community-service/src
COPY notification-service/src notification-service/src

# Build every module in the reactor (tests skipped — the notification
# integration test requires a Docker daemon, and unit tests add little
# to an image build; run `mvn test` in CI instead).
RUN mvn clean package -DskipTests


# ==============================
# Stage 2: Runtime — all service JARs, select one via SERVICE_NAME
# ==============================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy every service JAR
COPY --from=build /workspace/eureka-server/target/eureka-server-1.0.0.jar /app/
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0.jar /app/
COPY --from=build /workspace/auth-service/target/auth-service-1.0.0.jar /app/
COPY --from=build /workspace/user-service/target/user-service-1.0.0.jar /app/
COPY --from=build /workspace/complaint-service/target/complaint-service-1.0.0.jar /app/
COPY --from=build /workspace/community-service/target/community-service-1.0.0.jar /app/
COPY --from=build /workspace/notification-service/target/notification-service-1.0.0.jar /app/

# All service ports:
#   eureka-server 8761 · api-gateway 8080 · auth-service 8081 · user-service 8082
#   complaint-service 8083 · community-service 8084 · notification-service 8085
EXPOSE 8761 8080 8081 8082 8083 8084 8085

# Run the service selected by the SERVICE_NAME environment variable
# (defaults to eureka-server since it must be up before anything else).
ENTRYPOINT ["sh", "-c", "exec java -jar /app/${SERVICE_NAME:-eureka-server}-1.0.0.jar"]
