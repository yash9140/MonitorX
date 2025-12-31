# ============================================
# Multi-Stage Build for Spring Boot on Render
# Optimized for 512MB RAM constraint
# ============================================

# ============================================
# Stage 1: Build Stage
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace/app

# Install required tools
RUN apk add --no-cache wget unzip

# Download and install Gradle
RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip -q gradle-8.5-bin.zip && \
    rm gradle-8.5-bin.zip

ENV GRADLE_HOME=/workspace/app/gradle-8.5
ENV PATH=$PATH:$GRADLE_HOME/bin

# Copy Gradle configuration files FIRST (for caching)
COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Pre-download dependencies (cached layer)
# This layer is cached unless build files change
RUN gradle dependencies --no-daemon --console=plain || true

# Copy application source
COPY src src

# Build the JAR with optimizations
# -x test: Skip test execution (tests should run in CI/CD)
# -x check: Skip static analysis
# --no-daemon: Don't start background process
# --console=plain: Reduce output overhead
RUN gradle bootJar -x test -x check \
    --no-daemon \
    --console=plain \
    --stacktrace

# ============================================
# Stage 2: Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Add metadata
LABEL maintainer="MonitorX Team"
LABEL version="1.0.0"
LABEL description="MonitorX API Monitoring Backend"

WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder --chown=appuser:appgroup \
    /workspace/app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# JVM optimization for 512MB container
# Total container RAM: 512MB
# JVM heap: 384MB (75%)
# Metaspace: 96MB
# Direct memory: 32MB
# = ~512MB total
ENV JAVA_OPTS="\
    -Xms128m \
    -Xmx384m \
    -XX:MaxMetaspaceSize=96m \
    -XX:MaxDirectMemorySize=32m \
    -XX:+UseSerialGC \
    -XX:+TieredCompilation \
    -XX:TieredStopAtLevel=1 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
