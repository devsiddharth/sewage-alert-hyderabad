# Build stage - standalone Maven build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy only what ai-service needs: root pom + its own pom
COPY pom.xml .
COPY ai-service/pom.xml ai-service/pom.xml

# Copy ai-service source
COPY ai-service/src ai-service/src

# Build ai-service standalone (not as part of reactor, since it's not in root modules)
RUN mvn package -f ai-service/pom.xml -DskipTests -B -q

# ---- Runtime ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/ai-service/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
