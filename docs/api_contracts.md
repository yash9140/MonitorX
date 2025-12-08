# MonitorX API Contracts

This document describes all API endpoints, request/response formats, and authentication requirements for the MonitorX platform.

## Base URL

```
http://localhost:8080
```

## Authentication

All endpoints except `/auth/**` and `/actuator/**` require JWT authentication.

**Authentication Header:**
```
Authorization: Bearer <JWT_TOKEN>
```

---

## Authentication Endpoints

### POST /auth/signup

Create a new user account.

**Request Body:**
```json
{
  "username": "string (3-50 characters, required)",
  "email": "string (valid email, required)",
  "password": "string (min 6 characters, required)",
  "role": "string (optional, default: 'developer')"
}
```

**Success Response (201):**
```json
{
  "id": "string",
  "username": "string",
  "email": "string",
  "role": "string"
}
```

**Error Responses:**
- 400: Validation error or email/username already exists
- 500: Internal server error

---

### POST /auth/login

Authenticate and receive JWT token.

**Request Body:**
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200):**
```json
{
  "token": "string (JWT token)",
  "user": {
    "id": "string",
    "username": "string",
    "email": "string",
    "role": "string"
  }
}
```

**Error Responses:**
- 400: Invalid credentials
- 500: Internal server error

---

## Collector Endpoints (Protected)

### POST /collector/logs

Ingest API log data from tracking clients.

**Request Body:**
```json
{
  "serviceName": "string (required)",
  "method": "string (required, e.g., 'GET', 'POST')",
  "endpoint": "string (required)",
  "timestamp": "string (ISO 8601 format, optional, defaults to now)",
  "latency": "number (milliseconds, required)",
  "statusCode": "number (required)",
  "requestSize": "number (bytes, optional, default: 0)",
  "responseSize": "number (bytes, optional, default: 0)"
}
```

**Success Response (201):**
```json
{
  "id": "string",
  "serviceName": "string",
  "method": "string",
  "endpoint": "string",
  "timestamp": "string",
  "latency": "number",
  "statusCode": "number",
  "requestSize": "number",
  "responseSize": "number"
}
```

---

### POST /collector/rate-limit-events

Record rate limit violation events.

**Request Body:**
```json
{
  "serviceName": "string (required)",
  "currentRate": "number (requests per second, required)",
  "limit": "number (configured limit, required)"
}
```

**Success Response (201):**
```json
{
  "id": "string",
  "serviceName": "string",
  "timestamp": "string",
  "currentRate": "number",
  "limit": "number"
}
```

---

## Logs Endpoint (Protected)

### GET /logs

Query logs with filters and pagination.

**Query Parameters:**
- `serviceName` (string, optional): Filter by service name
- `endpoint` (string, optional): Filter by endpoint (regex supported)
- `minStatusCode` (number, optional): Minimum status code
- `maxStatusCode` (number, optional): Maximum status code
- `slowOnly` (boolean, optional): Show only slow APIs (latency > 500ms)
- `brokenOnly` (boolean, optional): Show only 5xx errors
- `startDate` (string ISO 8601, optional): Start date for filtering
- `endDate` (string ISO 8601, optional): End date for filtering
- `page` (number, optional, default: 0): Page number
- `size` (number, optional, default: 20): Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": "string",
      "serviceName": "string",
      "method": "string",
      "endpoint": "string",
      "timestamp": "string",
      "latency": "number",
      "statusCode": "number",
      "requestSize": "number",
      "responseSize": "number"
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number (current page)"
}
```

---

## Alerts Endpoint (Protected)

### GET /alerts

Query alerts with filters.

**Query Parameters:**
- `alertType` (string, optional): SLOW_API | BROKEN_API | RATE_LIMIT
- `serviceName` (string, optional): Filter by service
- `startDate` (string ISO 8601, optional): Start date
- `endDate` (string ISO 8601, optional): End date
- `page` (number, optional, default: 0): Page number
- `size` (number, optional, default: 20): Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": "string",
      "serviceName": "string",
      "endpoint": "string",
      "alertType": "SLOW_API | BROKEN_API | RATE_LIMIT",
      "reason": "string",
      "timestamp": "string"
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number"
}
```

---

## Issues Endpoints (Protected)

### GET /issues

Query issues with filters.

**Query Parameters:**
- `status` (string, optional): OPEN | RESOLVED
- `serviceName` (string, optional): Filter by service
- `issueType` (string, optional): SLOW_API | BROKEN_API | RATE_LIMIT
- `page` (number, optional, default: 0): Page number
- `size` (number, optional, default: 20): Page size

**Success Response (200):**
```json
{
  "content": [
    {
      "id": "string",
      "serviceName": "string",
      "endpoint": "string",
      "issueType": "SLOW_API | BROKEN_API | RATE_LIMIT",
      "status": "OPEN | RESOLVED",
      "hitCount": "number",
      "firstSeenAt": "string",
      "lastSeenAt": "string",
      "resolvedAt": "string (nullable)",
      "resolvedBy": "string (nullable)",
      "version": "number (for optimistic locking)"
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number"
}
```

---

### GET /issues/{id}

Get a specific issue by ID.

**Path Parameters:**
- `id` (string, required): Issue ID

**Success Response (200):**
```json
{
  "id": "string",
  "serviceName": "string",
  "endpoint": "string",
  "issueType": "SLOW_API | BROKEN_API | RATE_LIMIT",
  "status": "OPEN | RESOLVED",
  "hitCount": "number",
  "firstSeenAt": "string",
  "lastSeenAt": "string",
  "resolvedAt": "string (nullable)",
  "resolvedBy": "string (nullable)",
  "version": "number"
}
```

---

### PUT /issues/{id}/resolve

Mark an issue as resolved.

**Path Parameters:**
- `id` (string, required): Issue ID

**Request Body:**
```json
{
  "resolvedBy": "string (username, required)"
}
```

**Success Response (200):**
```json
{
  "id": "string",
  "serviceName": "string",
  "endpoint": "string",
  "issueType": "SLOW_API | BROKEN_API | RATE_LIMIT",
  "status": "RESOLVED",
  "hitCount": "number",
  "firstSeenAt": "string",
  "lastSeenAt": "string",
  "resolvedAt": "string",
  "resolvedBy": "string",
  "version": "number"
}
```

**Error Responses:**
- 404: Issue not found
- 409: Concurrent modification (optimistic locking failure)

---

## Stats Endpoints (Protected)

### GET /stats/summary

Get all dashboard statistics in one call.

**Success Response (200):**
```json
{
  "slowApiCount": "number",
  "brokenApiCount": "number",
  "rateLimitViolations": "number",
  "averageLatency": "number"
}
```

---

### GET /stats/slow-api-count

Get count of slow APIs (latency > 500ms).

**Success Response (200):**
```json
{
  "count": "number"
}
```

---

### GET /stats/broken-api-count

Get count of broken APIs (5xx status).

**Success Response (200):**
```json
{
  "count": "number"
}
```

---

### GET /stats/rate-limit-violations

Get count of rate limit violations.

**Success Response (200):**
```json
{
  "count": "number"
}
```

---

### GET /stats/average-latency

Get average latency across all APIs.

**Success Response (200):**
```json
{
  "latency": "number (milliseconds)"
}
```

---

### GET /stats/top-slow-endpoints

Get top N slowest endpoints.

**Query Parameters:**
- `limit` (number, optional, default: 10): Number of endpoints to return

**Success Response (200):**
```json
[
  {
    "serviceName": "string",
    "endpoint": "string",
    "averageLatency": "number",
    "hitCount": "number"
  }
]
```

---

### GET /stats/error-rate-time-series

Get error rate time series data.

**Query Parameters:**
- `hours` (number, optional, default: 24): Number of hours to look back

**Success Response (200):**
```json
[
  {
    "timestamp": "string",
    "errorCount": "number",
    "totalCount": "number",
    "errorRate": "number (0.0 to 1.0)"
  }
]
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "string (ISO 8601)",
  "status": "number (HTTP status code)",
  "error": "string (error type)",
  "message": "string (error description)",
  "details": {
    "field1": "validation message",
    "field2": "validation message"
  }
}
```

## HTTP Status Codes

- **200 OK**: Successful GET request
- **201 Created**: Successful POST request
- **400 Bad Request**: Invalid input or validation error
- **401 Unauthorized**: Missing or invalid JWT token
- **404 Not Found**: Resource not found
- **409 Conflict**: Concurrent modification or state conflict
- **500 Internal Server Error**: Server-side error
