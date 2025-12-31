# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Install Gradle
RUN apk add --no-cache wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    rm gradle-8.5-bin.zip

ENV GRADLE_HOME=/app/gradle-8.5
ENV PATH=$PATH:$GRADLE_HOME/bin

# Copy build files
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies with memory constraints
RUN gradle dependencies --no-daemon --max-workers=2 \
    -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m" \
    || true

# Copy source code
COPY src src

# Build application with memory optimizations for Render free tier
# Try with 512MB first, fallback to 384MB if it fails
RUN gradle bootJar --no-daemon --max-workers=2 \
    -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+UseSerialGC" \
    || gradle bootJar --no-daemon --max-workers=1 \
    -Dorg.gradle.jvmargs="-Xmx384m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC"

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set JVM options for Render free tier (512MB RAM limit)
# Use Serial GC for lower memory footprint
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
