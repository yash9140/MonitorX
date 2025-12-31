# Render bootJar Build Failure - Debug & Fix Guide

## 🔍 Root Cause Analysis

### Common Causes of `bootJar` Failure (Tests Skipped)

1. **Compilation Errors**
   - Kotlin source files fail to compile
   - Missing dependencies
   - Annotation processing failures

2. **Memory Constraints**
   - Gradle process OOM during compilation
   - Kotlin compiler exceeds limits
   - Metaspace exhaustion

3. **Gradle/Java Version Mismatch**
   - Gradle 8.5 requires Java 17+
   - Kotlin 1.9.20 compatible with Java 17
   - Spring Boot 3.2.0 requires Java 17+
   - **Your setup: All aligned to Java 17** ✅

4. **Missing Gradle Wrapper**
   - gradle-wrapper.jar missing from repo
   - Causes gradlew to fail silently

## 🐛 Debug Steps for Render

### Step 1: Check Build Logs on Render

Look for:
```
> Task :compileKotlin FAILED
> Task :bootJar FAILED
FAILURE: Build failed with an exception
```

### Step 2: Identify Specific Error

Common patterns:
- `OutOfMemoryError` → Memory issue
- `Compilation error` → Source code problem  
- `Could not resolve` → Dependency issue
- `ClassNotFoundException` → Wrapper issue

### Step 3: Local Docker Build Test

```bash
cd backend
docker build -t test-backend .
```

If this fails locally, you'll see the actual error!

## ✅ Solution Implemented

### Fixed Dockerfile

**Key changes:**
1. Uses Gradle wrapper (`./gradlew`) instead of downloading Gradle
2. Added `--info` flag for verbose logging
3. Enhanced error output on failure
4. Proper caching layers

### Gradle Wrapper Verification

**Required files:**
```
backend/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar    ← MUST exist!
│       └── gradle-wrapper.properties
├── gradlew
└── gradlew.bat
```

### If Wrapper Missing

Regenerate wrapper:
```bash
cd backend
gradle wrapper --gradle-version=8.5
```

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Gradle wrapper jar exists: `backend/gradle/wrapper/gradle-wrapper.jar`
- [ ] Test local Docker build: `docker build -t test ./backend`
- [ ] Verify JAR created: `build/libs/*.jar`

### Render Configuration  
- [ ] Root Directory: `backend`
- [ ] Dockerfile Path: `./Dockerfile`
- [ ] Environment variables set (MongoDB URIs, JWT secret)

### Post-Deployment
- [ ] Monitor Render build logs for errors
- [ ] Check bootJar task completes: "BUILD SUCCESSFUL"
- [ ] Verify service starts: Check logs for "Started MonitorXApplication"
- [ ] Test health endpoint

## 🚨 If Build Still Fails

### Fallback 1: Add Build Cache

In `gradle.properties`:
```properties
org.gradle.caching=true
```

### Fallback 2: Simplify Dockerfile

Remove dependency pre-download if causing issues:
```dockerfile
# Comment out this line:
# RUN ./gradlew dependencies --no-daemon --console=plain || echo "..."
```

### Fallback 3: Check for Line Ending Issues

Windows CRLF can break gradlew on Linux:
```bash
dos2unix gradlew
```

## 🎯 Expected Outcome

**Successful build shows:**
```
> Task :compileKotlin
> Task :compileJava
> Task :processResources
> Task :classes
> Task :bootJar

BUILD SUCCESSFUL in 8m 32s
```

**JAR file:** `build/libs/monitorx-backend-1.0.0.jar` (~40-80MB)

**Total build time:** 6-10 minutes (first), 3-4 minutes (cached)
