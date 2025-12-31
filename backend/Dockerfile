# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy gradle wrapper files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .

# Make gradlew executable and ensure Unix line endings
RUN chmod +x gradlew && \
    dos2unix gradlew 2>/dev/null || sed -i 's/\r$//' gradlew

# Copy build files
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (with memory limits for free tier)
RUN sh gradlew dependencies --no-daemon --max-workers=2 || true

# Copy source code
COPY src src

# Build application with memory optimizations for Render free tier
RUN sh gradlew bootJar --no-daemon --max-workers=2 \
    -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m" \
    || sh gradlew bootJar --no-daemon --max-workers=1 \
    -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=192m"

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set JVM options for Render free tier (512MB limit)
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:MaxMetaspaceSize=128m"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
