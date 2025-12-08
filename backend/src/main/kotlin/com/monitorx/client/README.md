# MonitorX API Tracking Client

## Configuration

Add the following properties to your `application.yml`:

```yaml
monitorx:
  client:
    enabled: true
    service-name: my-awesome-service
    collector-url: http://localhost:8080
    rate-limit: 100  # requests per second
    rate-limit-enabled: true
```

## Usage

1. Add dependency to your `build.gradle.kts` or copy the client package
2. Configure properties in `application.yml`
3. The tracking happens automatically via interceptors

## Features

- **Automatic API Tracking**: Captures method, endpoint, latency, status code
- **Rate Limit Monitoring**: Non-blocking tracking when limits exceeded
- **Async Transmission**: Doesn't impact main request performance
- **Configurable**: Enable/disable per environment
