# ==========================================
# Phase 1: Build Package Compilation
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency files first (caching layer)
COPY pom.xml .
COPY src ./src

# Compile and package production artifact jar (skip test executions for speed)
RUN mvn clean package -DskipTests

# ==========================================
# Phase 2: Lightweight Production Execution
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security hardening
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built JAR from Phase 1 build stage
COPY --from=build /app/target/trackflow-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
