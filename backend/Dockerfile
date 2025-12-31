# ==================================================================
# Production Dockerfile for Spring Boot + Kotlin on Render Free Tier  
# Handles missing Gradle wrapper by installing Gradle directly
# ==================================================================

# Stage 1: Build
FROM eclipse-te murin:17-jdk-alpine AS build

WORKDIR /build

# Install build tools
RUN apk add --no-cache wget unzip

# Download and install Gradle 8.5
RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip -q gradle-8.5-bin.zip && \
    rm gradle-8.5-bin.zip

ENV GRADLE_HOME=/build/gradle-8.5
ENV PATH=$PATH:$GRADLE_HOME/bin

# Copy Gradle configuration (memory limits applied)
COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon --console=plain 2>&1 | head -100 || echo "Partial dependency download"

# Copy source code
COPY src src

# Build with enhanced error reporting
# The --info flag will show compilation errors clearly
RUN gradle bootJar -x test -x check \
    --no-daemon \
    --console=plain \
    --info \
    2>&1 | tee /build/build.log && echo "Build completed" \
    || (echo "===== BUILD FAILED =====" && \
    echo "Last 100 lines of build log:" && \
    tail -100 /build/build.log && \
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

# Runtime JVM optimizations for 512MB container
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
