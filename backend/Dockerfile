# ==================================================================
# Production Dockerfile for Spring Boot + Kotlin on Render Free Tier
# ==================================================================

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /build

# Copy Gradle wrapper files (must exist in repo)
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .

# Ensure gradlew is executable
RUN chmod +x ./gradlew

# Copy build configuration
COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies first (cached layer)
RUN ./gradlew dependencies --no-daemon --console=plain || echo "Dependencies partial download"

# Copy source code
COPY src src

# Build with verbose logging for debugging
RUN ./gradlew bootJar -x test -x check \
    --no-daemon \
    --console=plain \
    --stacktrace \
    --info \
    || (echo "=== BUILD FAILED ===" && \
    echo "=== Showing last 50 lines of output ===" && \
    exit 1)

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Runtime dependencies
RUN apk add --no-cache curl && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup

# Copy JAR
COPY --from=build --chown=appuser:appgroup /build/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

# Runtime JVM settings for 512MB container
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
