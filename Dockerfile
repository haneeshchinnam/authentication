# Stage 1: Build application
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and configuration files first for dependency caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execution permission to gradlew and download dependencies
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source code and build executable boot jar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime application environment
FROM eclipse-temurin:17-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
USER appuser

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
