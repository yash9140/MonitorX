# MonitorX - API Monitoring & Observability Platform

<div align="center">

![MonitorX](https://img.shields.io/badge/MonitorX-Premium%20SaaS-6366f1?style=for-the-badge)
![Version](https://img.shields.io/badge/version-1.0.0-34d399?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-0ea5e9?style=for-the-badge)

**A premium SaaS platform for real-time API monitoring, performance tracking, and intelligent alerting**

[Features](#-features) • [Architecture](#-architecture) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [API Reference](#-api-reference)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Frontend Guide](#-frontend-guide)
- [Backend Guide](#-backend-guide)
- [Database Design](#-database-design)
- [Security](#-security)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Overview

MonitorX is a production-grade API monitoring and observability platform built with premium SaaS aesthetics. It provides real-time insights into your API ecosystem, automatic issue detection, and intelligent alerting to help teams maintain high-performing, reliable APIs.

### Key Capabilities

- **Real-time Monitoring**: Track API latency, error rates, and throughput in milliseconds
- **Intelligent Alerting**: Automated detection of slow APIs, broken endpoints, and rate limit violations
- **Issue Management**: Automatic issue creation, tracking, and resolution workflows
- **Premium UI/UX**: Glassmorphism design, gradient backgrounds, and smooth animations
- **Scalable Architecture**: Dual MongoDB setup for logs and metadata, asynchronous processing

---

## ✨ Features

### Core Functionality

#### 🔍 **Real-time API Log Ingestion**
- High-throughput log collection endpoint (`POST /collector/logs`)
- Automatic metadata extraction (latency, status codes, timestamps)
- Dual-database architecture for optimized storage and querying

#### 📊 **Performance Metrics**
- **Slow API Detection**: Identifies APIs with latency > 500ms
- **Error Rate Tracking**: Monitors 5xx errors across services
- **Average Latency**: Real-time latency aggregation
- **Top Slow Endpoints**: Ranked list of performance bottlenecks

#### 🚨 **Smart Alerting System**
- Automatic alert generation for:
  - Slow APIs (latency > 500ms)
  - Broken APIs (status >= 500)
  - Rate limit violations
- Configurable thresholds and notification channels

#### 🐛 **Issue Tracking**
- Automated issue creation from alerts
- Deduplication logic (one issue per service+endpoint+type)
- Optimistic locking for concurrent updates
- Resolution workflows with version tracking

#### 📈 **Data Visualization**
- Interactive charts powered by Recharts
- Error rate time series (24-hour view)
- Top 10 slow endpoints bar chart
- Real-time dashboard with auto-refresh

---

## 🛠 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0 with Kotlin 1.9
- **Database**: MongoDB (Dual instances for logs & metadata)
- **Authentication**: JWT (HS512) with token-based auth
- **Build Tool**: Gradle Kotlin DSL
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, MockK

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS with custom design system
- **Charts**: Recharts for data visualization
- **HTTP Client**: Axios
- **State Management**: React Hooks (useState, useEffect)

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Databases**: 
  - MongoDB 7.0 (logs_db) - API logs storage
  - MongoDB 7.0 (metadata_db) - Users, alerts, issues
- **Networking**: Docker bridge network with service discovery

---

## 🏗 Architecture

### System Architecture

```
┌─────────────────┐
│  Next.js Client │
│   (Port 3000)   │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────────────────────┐
│   Spring Boot Backend (8080)    │
│  ┌──────────────────────────┐   │
│  │  Security Layer (JWT)    │   │
│  ├──────────────────────────┤   │
│  │  Controllers             │   │
│  │  - Auth, Stats, Logs     │   │
│  │  - Alerts, Issues        │   │
│  ├──────────────────────────┤   │
│  │  Services                │   │
│  │  - Async Processing      │   │
│  │  - Alerting Engine       │   │
│  │  - Issue Management      │   │
│  └──────────────────────────┘   │
└─────────┬───────────────────┬───┘
          │                   │
   ┌──────▼──────┐     ┌─────▼─────┐
   │ MongoDB     │     │ MongoDB   │
   │ logs_db     │     │metadata_db│
   │ (Port 27017)│     │(Port 27018│
   └─────────────┘     └───────────┘
```

### Data Flow

1. **Client Request** → Frontend sends authenticated requests
2. **JWT Validation** → Backend validates token
3. **Log Ingestion** → Logs stored in `logs_db`
4. **Async Processing** → AlertingService processes logs
5. **Alert Generation** → Alerts created in `metadata_db`
6. **Issue Creation** → Issues auto-created/updated
7. **Dashboard Update** → Real-time metrics aggregation

---

## 📁 Project Structure

```
MonitorX/
├── backend/                          # Kotlin/Spring Boot backend
│   ├── src/main/kotlin/com/monitorx/
│   │   ├── config/                   # Configuration classes
│   │   │   ├── MongoConfig.kt        # Dual MongoDB setup
│   │   │   └── SecurityConfig.kt     # JWT security config
│   │   ├── controller/               # REST controllers
│   │   │   ├── AuthController.kt     # Login/Signup
│   │   │   ├── CollectorController.kt # Log ingestion
│   │   │   ├── StatsController.kt    # Metrics API
│   │   │   ├── LogsController.kt     # Log queries
│   │   │   ├── AlertsController.kt   # Alert management
│   │   │   └── IssuesController.kt   # Issue tracking
│   │   ├── model/                    # Data models
│   │   │   ├── User.kt
│   │   │   ├── ApiLog.kt
│   │   │   ├── Alert.kt
│   │   │   └── Issue.kt
│   │   ├── repository/               # MongoDB repositories
│   │   │   ├── logs/                 # Logs DB repos
│   │   │   └── metadata/             # Metadata DB repos
│   │   ├── service/                  # Business logic
│   │   │   ├── AuthService.kt
│   │   │   ├── AlertingService.kt    # Alert detection
│   │   │   ├── IssueService.kt       # Issue management
│   │   │   ├── StatsService.kt       # Metrics aggregation
│   │   │   └── QueryService.kt       # Dynamic queries
│   │   ├── security/
│   │   │   └── JwtAuthenticationFilter.kt
│   │   ├── dto/                      # Data Transfer Objects
│   │   │   ├── AuthDtos.kt
│   │   │   └── DataDtos.kt
│   │   └── util/
│   │       └── JwtUtil.kt            # JWT token handling
│   ├── src/main/resources/
│   │   └── application.yml           # App configuration
│   ├── build.gradle.kts              # Gradle build config
│   └── Dockerfile                    # Backend container
│
├── frontend/                         # Next.js frontend
│   ├── app/                          # App router pages
│   │   ├── page.tsx                  # Landing page
│   │   ├── login/page.tsx            # Login page
│   │   ├── signup/page.tsx           # Signup page
│   │   ├── dashboard/                # Dashboard
│   │   │   ├── layout.tsx            # Sidebar layout
│   │   │   └── page.tsx              # Dashboard page
│   │   ├── logs/                     # Logs page
│   │   │   ├── layout.tsx
│   │   │   └── page.tsx
│   │   ├── alerts/                   # Alerts page
│   │   │   ├── layout.tsx
│   │   │   └── page.tsx
│   │   ├── issues/                   # Issues page
│   │   │   ├── layout.tsx
│   │   │   └── page.tsx
│   │   └── globals.css               # Global styles + design system
│   ├── components/                   # Reusable components
│   │   ├── Sidebar.tsx               # Navigation sidebar
│   │   ├── StatsCard.tsx             # KPI cards
│   │   ├── TopSlowEndpointsChart.tsx # Bar chart
│   │   └── ErrorRateChart.tsx        # Area chart
│   ├── lib/                          # Utilities
│   │   ├── auth.ts                   # Auth helpers
│   │   ├── api.ts                    # API client
│   │   └── stats.ts                  # Stats API
│   ├── middleware.ts                 # Route protection
│   ├── package.json
│   └── tailwind.config.ts            # Tailwind configuration
│
├── docker-compose.yml                # Multi-container orchestration
└── README.md                         # This file
```

---

## 🚀 Quick Start

### Prerequisites

- **Docker** & **Docker Compose** (required)
- **Node.js** 18+ (for local frontend development)
- **Java** 17+ (for local backend development)
- **Gradle** 8+ (usually bundled with project)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd MonitorX
```

### 2. Start All Services

```bash
docker-compose up --build
```

This command will:
- Build the backend Docker image
- Build the frontend Docker image
- Start 2 MongoDB instances (ports 27017, 27018)
- Start the backend service (port 8080)
- Start the frontend service (port 3000)

### 3. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### 4. Create Your First User

```powershell
# Using PowerShell
$signup = @{
    username = "admin"
    email = "admin@monitorx.com"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/signup" `
    -Method POST `
    -Body $signup `
    -ContentType "application/json"
```

### 5. Login and Get Token

```powershell
$login = @{
    email = "admin@monitorx.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method POST `
    -Body $login `
    -ContentType "application/json"

$token = $response.token
Write-Host "Token: $token"
```

### 6. Ingest Sample Logs

```powershell
# Sample slow API log
$log = @{
    serviceName = "payment-service"
    method = "POST"
    endpoint = "/api/payments/process"
    statusCode = 200
    latency = 850
    requestSize = 2048
    responseSize = 1024
    timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/collector/logs" `
    -Method POST `
    -Body $log `
    -ContentType "application/json" `
    -Headers @{Authorization="Bearer $token"}
```

---

## ⚙️ Configuration

### Backend Configuration (`application.yml`)

```yaml
spring:
  data:
    mongodb:
      # Logs Database
      uri: mongodb://mongo-logs:27017/logs_db
      # Metadata Database  
      metadata-uri: mongodb://mongo-metadata:27018/metadata_db

security:
  jwt:
    secret: your-secret-key-here-change-in-production
    expiration: 86400000  # 24 hours in milliseconds

server:
  port: 8080

cors:
  allowed-origins: http://localhost:3000
```

### Frontend Configuration

Environment variables (create `.env.local`):

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 📡 API Documentation

### Authentication Endpoints

#### POST `/auth/signup`
Create a new user account.

**Request:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "jwt-token-here",
  "user": {
    "id": "string",
    "username": "string",
    "email": "string"
  }
}
```

#### POST `/auth/login`
Authenticate and receive JWT token.

**Request:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "jwt-token-here",
  "user": {
    "id": "string",
    "username": "string",
    "email": "string"
  }
}
```

### Data Collection Endpoints

#### POST `/collector/logs`
Ingest API request logs. **Requires JWT Authentication.**

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request:**
```json
{
  "serviceName": "string",
  "method": "GET|POST|PUT|DELETE",
  "endpoint": "string",
  "statusCode": number,
  "latency": number,
  "requestSize": number,
  "responseSize": number,
  "timestamp": "ISO8601 string"
}
```

**Response:**
```json
{
  "id": "generated-log-id"
}
```

#### POST `/collector/rate-limit`
Report rate limit violations. **Requires JWT Authentication.**

**Request:**
```json
{
  "serviceName": "string",
  "endpoint": "string",
  "clientIp": "string",
  "timestamp": "ISO8601 string"
}
```

### Statistics Endpoints

#### GET `/stats/summary`
Get dashboard summary metrics. **Requires JWT Authentication.**

**Response:**
```json
{
  "slowApiCount": number,
  "brokenApiCount": number,
  "rateLimitViolations": number,
  "averageLatency": number,
  "topSlowEndpoints": [
    {
      "serviceName": "string",
      "endpoint": "string",
      "averageLatency": number,
      "requestCount": number
    }
  ],
  "errorRateTimeSeries": [
    {
      "timestamp": "ISO8601 string",
      "errorCount": number,
      "totalCount": number,
      "errorRate": number
    }
  ]
}
```

### Logs Query Endpoints

#### POST `/logs`
Query API logs with filters. **Requires JWT Authentication.**

**Request:**
```json
{
  "serviceName": "string (optional)",
  "endpoint": "string (optional)",
  "statusCode": number (optional),
  "minLatency": number (optional),
  "slowOnly": boolean (optional),
  "brokenOnly": boolean (optional),
  "page": number (default: 0),
  "size": number (default: 20)
}
```

**Response:**
```json
{
  "content": [...logs],
  "totalElements": number,
  "totalPages": number,
  "currentPage": number
}
```

### Full API Documentation

Visit **http://localhost:8080/swagger-ui.html** for interactive API documentation with request/response examples.

---

## 🎨 Frontend Guide

### Design System

MonitorX uses a premium SaaS design system with:

- **Dark Theme**: Background `#0A0D12`
- **Glassmorphism**: `bg-white/5 backdrop-blur-xl`
- **Gradients**: Indigo → Sky → Emerald
- **Typography**: Inter font, semibold headings

### Reusable CSS Classes

```css
.glass-panel       /* Glassmorphism cards */
.gradient-text     /* Gradient text effect */
.btn-primary       /* Primary CTA button */
.btn-secondary     /* Secondary button */
.input-field       /* Form input styling */
.badge-success     /* Success badge */
.badge-error       /* Error badge */
```

### Component Library

- **`<Sidebar />`**: Navigation sidebar with active states
- **`<StatsCard />`**: KPI cards with icons and gradients
- **`<TopSlowEndpointsChart />`**: Bar chart for slow endpoints
- **`<ErrorRateChart />`**: Area chart for error rates

### Authentication Flow

1. User visits protected route → Redirected to `/login`
2. Submits credentials → Backend validates
3. Receives JWT token → Stored in localStorage
4. Token included in all API requests via Axios interceptor
5. Logout clears token and redirects to landing page

---

## 🔧 Backend Guide

### Dual MongoDB Configuration

```kotlin
@Configuration
class MongoConfig {
    @Bean("logsMongoTemplate")
    fun logsMongoTemplate(): MongoTemplate {
        // Connected to logs_db (port 27017)
    }
    
    @Bean("metadataMongoTemplate")
    fun metadataMongoTemplate(): MongoTemplate {
        // Connected to metadata_db (port 27018)
    }
}
```

### Alerting Engine

The `AlertingService` runs asynchronously on log ingestion:

```kotlin
@Async
fun processLog(log: ApiLog) {
    // Check for slow API
    if (log.latency > 500) {
        createAlert(SLOW_API, log)
        createOrUpdateIssue(SLOW_API, log)
    }
    
    // Check for broken API
    if (log.statusCode >= 500) {
        createAlert(BROKEN_API, log)
        createOrUpdateIssue(BROKEN_API, log)
    }
}
```

### Issue Deduplication

Issues use a compound index to ensure uniqueness:

```kotlin
@CompoundIndex(
    name = "unique_open_issue",
    def = "{'serviceName': 1, 'endpoint': 1, 'type': 1, 'status': 1}",
    unique = true,
    partialFilterExpression = "{'status': 'OPEN'}"
)
```

Only one OPEN issue per `(serviceName, endpoint, type)` combination.

---

## 🗄 Database Design

### Collections in `logs_db`

#### `api_logs`
```json
{
  "_id": "ObjectId",
  "serviceName": "string",
  "method": "string",
  "endpoint": "string",
  "statusCode": number,
  "latency": number,
  "requestSize": number,
  "responseSize": number,
  "timestamp": "ISODate"
}
```

**Indexes:**
- `{ timestamp: -1 }` - For time-range queries
- `{ serviceName: 1, endpoint: 1 }` - For service filtering
- `{ latency: 1 }` - For slow API detection
- `{ statusCode: 1 }` - For error filtering

#### `rate_limit_events`
```json
{
  "_id": "ObjectId",
  "serviceName": "string",
  "endpoint": "string",
  "clientIp": "string",
  "timestamp": "ISODate"
}
```

### Collections in `metadata_db`

#### `users`
```json
{
  "_id": "ObjectId",
  "username": "string",
  "email": "string",
  "passwordHash": "bcrypt hash"
}
```

#### `alerts`
```json
{
  "_id": "ObjectId",
  "type": "SLOW_API | BROKEN_API | RATE_LIMIT",
  "serviceName": "string",
  "endpoint": "string",
  "message": "string",
  "severity": "LOW | MEDIUM | HIGH | CRITICAL",
  "metadata": {},
  "timestamp": "ISODate",
  "acknowledged": boolean
}
```

#### `issues`
```json
{
  "_id": "ObjectId",
  "type": "SLOW_API | BROKEN_API | RATE_LIMIT",
  "serviceName": "string",
  "endpoint": "string",
  "status": "OPEN | RESOLVED",
  "occurrenceCount": number,
  "firstOccurrence": "ISODate",
  "lastOccurrence": "ISODate",
  "resolvedAt": "ISODate (optional)",
  "resolvedBy": "string (optional)",
  "version": number  // For optimistic locking
}
```

---

## 🔒 Security

### Authentication

- **JWT Tokens**: HS512 algorithm with configurable secret
- **Token Expiration**: 24 hours (configurable)
- **Password Hashing**: BCrypt with strength 12

### Authorization

Protected endpoints require `Authorization: Bearer <token>` header.

**Public Endpoints:**
- `POST /auth/login`
- `POST /auth/signup`

**Protected Endpoints:**
- All `/collector/*` endpoints
- All `/stats/*` endpoints
- All `/logs`, `/alerts`, `/issues` endpoints

### CORS Configuration

Configured in `SecurityConfig.kt`:

```kotlin
cors {
    allowedOrigins = ["http://localhost:3000"]
    allowedMethods = ["GET", "POST", "PUT", "DELETE"]
    allowedHeaders = ["*"]
}
```

---

## 🧪 Testing

### Backend Tests

```bash
cd backend
./gradlew test
```

Test coverage includes:
- Unit tests for services
- Integration tests for repositories  
- Controller tests with MockMvc

### Frontend Tests

```bash
cd frontend
npm test
```

---

## 🚢 Deployment

### Production Configuration

1. **Update JWT Secret**: Change in `application.yml`
2. **Update MongoDB URIs**: Point to production instances
3. **Update CORS Origins**: Add production frontend URL
4. **Environment Variables**: Set via Docker Compose or K8s

### Docker Compose Production

```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://prod-host:27017/logs_db
      - SECURITY_JWT_SECRET=${JWT_SECRET}
  # ... other services
```

### Health Checks

Backend exposes `/actuator/health` for monitoring:

```bash
curl http://localhost:8080/actuator/health
```

---

## 🐛 Troubleshooting

### Docker Issues

**Problem**: Port Already in Use
```bash
# Stop conflicting services
docker-compose down
# Or change ports in docker-compose.yml
```

**Problem**: Build Failures
```bash
# Clean rebuild
docker-compose build --no-cache
docker-compose up
```

### Backend Issues

**Problem**: MongoDB Connection Failed
- Check MongoDB containers are running: `docker ps`
- Verify URIs in `application.yml`
- Check network connectivity: `docker network inspect monitorx_default`

**Problem**: JWT Token Invalid
- Verify token format: `Authorization: Bearer <token>`
- Check token expiration
- Ensure secret matches between signup and login

### Frontend Issues

**Problem**: API Requests Failing
- Check backend is running: `curl http://localhost:8080/actuator/health`
- Verify CORS settings allow frontend origin
- Check browser console for detailed errors

**Problem**: Pages Not Loading
```bash
# Clear Next.js cache
rm -rf .next
npm run dev
```

---

## 📝 License

MIT License - See LICENSE file for details

---

## 👥 Contributors

Built with ❤️ for premium API monitoring

---

## 🙏 Acknowledgments

- Spring Boot framework
- Next.js team
- MongoDB
- Recharts library
- Tailwind CSS

---

**For Support**: Open an issue on GitHub or contact the development team
