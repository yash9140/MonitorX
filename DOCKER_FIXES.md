# Docker Build Fixes Applied

## Issues Fixed

### ✅ Issue 1: "./gradlew: not found"
**Solution:** Created proper Unix gradlew wrapper script and updated Dockerfile to copy it correctly.
- Added complete Unix gradlew script at `backend/gradlew`
- Updated Dockerfile to copy gradlew files BEFORE build files
- Added `chmod +x gradlew` to make it executable

### ✅ Issue 2: "wget: not found" 
**Solution:** Switched healthcheck from wget to curl and installed curl in runtime image.
- Changed `docker-compose.yml` healthcheck to use `curl -f`
- Added `RUN apk add --no-cache curl` to runtime stage in Dockerfile

### ✅ Issue 3: Collector service becomes unhealthy
**Solution:** Fixed by resolving Issue 2 (curl installation)

### ✅ Issue 4: docker-compose build fails during Gradle dependency download
**Solution:** Fixed gradlew copy order and permissions
- Copy gradlew wrapper files FIRST before build.gradle.kts
- Make gradlew executable with chmod
- This ensures `./gradlew dependencies` can run successfully

### ✅ Issue 5: Unix gradlew wrapper missing or not executable
**Solution:** Added complete Unix gradlew script
- Created full POSIX-compliant gradlew shell script
- Includes proper Java detection and error handling
- Compatible with Docker's Alpine Linux base image

## Files Modified

1. **backend/Dockerfile**
   - Reordered COPY commands (gradlew files first)
   - Added `chmod +x gradlew`
   - Added `RUN apk add --no-cache curl` in runtime stage

2. **docker-compose.yml**
   - Changed healthcheck from `wget` to `curl -f`

3. **backend/gradlew** (NEW)
   - Complete Unix shell script for Gradle wrapper
   - POSIX-compliant for maximum compatibility

## Verification Commands

All these commands should now work:

```powershell
# Build with Docker Compose
cd d:\MonitorX
docker-compose build

# Start all services
docker-compose up

# Check service health
docker-compose ps

# Local development (Windows)
cd backend
.\gradlew.bat bootRun

# Local development (Linux/Mac/Git Bash)
cd backend
./gradlew bootRun
```

## Docker Build Process

The Dockerfile now follows this optimized process:

1. **Build Stage (eclipse-temurin:17-jdk-alpine)**
   - Copy gradlew + gradlew.bat + gradle/ folder
   - Make gradlew executable (`chmod +x`)
   - Copy build files (build.gradle.kts, settings.gradle.kts)
   - Download dependencies (`./gradlew dependencies`)
   - Copy source code
   - Build JAR (`./gradlew bootJar`)

2. **Runtime Stage (eclipse-temurin:17-jre-alpine)**
   - Install curl for healthcheck
   - Copy JAR from build stage
   - Expose port 8080
   - Run application

## Healthcheck Details

The healthcheck now uses curl with proper flags:
```yaml
test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
```

- `-f` flag makes curl fail on HTTP errors
- Checks Spring Boot Actuator health endpoint
- Retries 3 times with 30s interval
- Allows 40s startup time before checking

## No Code Changes Required

✅ All backend Kotlin code remains unchanged
✅ All Spring Boot configurations unchanged
✅ All business logic unchanged
✅ All frontend code unchanged

Only infrastructure files were modified to fix Docker build and runtime issues.
