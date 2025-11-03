---
title: Rate-Limiting Configuration Guide
---

This document explains how to configure and extend the Bucket4j-based rate limiting using properties.

## Overview

```yaml
The rate limiting system supports two different strategies:

1. **AUTH Strategy**: For authentication endpoints, using strict time-based limits (per-minute, per-hour) to prevent brute-force attacks.
2. **BUSINESS Strategy**: For business endpoints, using plan-based limits for API usage quotas.

## Configuration in `application.yml`

### Configuration Structure

```yaml
application:
  rate-limit:
    enabled: true  # Enable/disable rate limiting globally

    # Configuration for authentication endpoints
    auth:
      enabled: true
      endpoints:
        - /api/auth/login
        - /api/auth/register
        - /api/auth/password/reset
        # ... more endpoints
      limits:
        # Short-term limit: prevents burst attacks
        - name: per-minute
          capacity: 10
          refill-tokens: 10
          refill-duration: 1m
        # Long-term limit: prevents sustained attacks
        - name: per-hour
          capacity: 100
          refill-tokens: 100
          refill-duration: 1h

    # Configuration for business endpoints
    business:
      enabled: true
      pricing-plans:
        free:
          name: free-plan
          capacity: 20
          refill-tokens: 20
          refill-duration: 1h
        basic:
          name: basic-plan
          capacity: 40
          refill-tokens: 40
          refill-duration: 1h
        professional:
          name: professional-plan
          capacity: 100
          refill-tokens: 100
          refill-duration: 1h
```

### Configuration Parameters

#### BandwidthLimit

Each limit (`BandwidthLimit`) supports the following parameters:

- **`name`** (String, required): Unique identifier for the limit, useful for logging and debugging.
- **`capacity`** (Long, required): Maximum number of tokens the bucket can hold.
- **`refill-tokens`** (Long, required): Number of tokens added on each refill.
- **`refill-duration`** (Duration, required): Duration between refills (e.g., `1m`, `1h`, `30s`).
- **`initial-tokens`** (Long, optional): Initial number of tokens in the bucket. Defaults to `capacity` if not provided.

#### Duration Formats

Spring Boot supports the following duration suffixes:

- `ns` - nanoseconds
- `us` - microseconds
- `ms` - milliseconds
- `s` - seconds
- `m` - minutes
- `h` - hours
- `d` - days

Examples: `30s`, `5m`, `1h`, `2d`

## Use Cases and Examples

### 1. Add a New Authentication Endpoint

To add a new endpoint that should be protected by authentication rate limiting:

```yaml
application:
  rate-limit:
    auth:
      endpoints:
        - /api/auth/login
        - /api/auth/register
        - /api/auth/new-endpoint  # New endpoint
```

### 2. Add a New Pricing Plan

To add a new pricing plan (e.g., "enterprise"):

```yaml
application:
  rate-limit:
    business:
      pricing-plans:
        free:
          name: free-plan
          capacity: 20
          refill-tokens: 20
          refill-duration: 1h
        enterprise:  # New plan
          name: enterprise-plan
          capacity: 1000
          refill-tokens: 1000
          refill-duration: 1h
```

Then update `PricingPlan.kt` to recognize the new plan:

```kotlin
fun resolvePlanFromApiKey(apiKey: String): PricingPlan {
    return when {
        apiKey.startsWith("EX001-") -> ENTERPRISE  // New condition
        apiKey.startsWith("PX001-") -> PROFESSIONAL
        apiKey.startsWith("BX001-") -> BASIC
        else -> FREE
    }
}
```

### 3. Adjust Limits for Different Environments

You can use Spring profiles to have different limits in development vs production:

**application-dev.yml:**

```yaml
application:
  rate-limit:
    auth:
      limits:
        - name: per-minute
          capacity: 100  # More permissive in dev
          refill-tokens: 100
          refill-duration: 1m
```

**application-prod.yml:**

```yaml
application:
  rate-limit:
    auth:
      limits:
        - name: per-minute
          capacity: 10  # Stricter in production
          refill-tokens: 10
          refill-duration: 1m
```

### 4. Layered Protection with Multiple Limits

Authentication endpoints may have multiple limits applied simultaneously:

```yaml
application:
  rate-limit:
    auth:
      limits:
        # Level 1: burst protection
        - name: burst-protection
          capacity: 5
          refill-tokens: 5
          refill-duration: 10s
        # Level 2: per-minute limit
        - name: per-minute
          capacity: 20
          refill-tokens: 20
          refill-duration: 1m
        # Level 3: per-hour limit
        - name: per-hour
          capacity: 100
          refill-tokens: 100
          refill-duration: 1h
```

An attacker must pass **all** configured limits for the request to be processed.

### 5. Temporarily Disable Rate-Limiting

To disable rate limiting entirely (e.g., for debugging):

```yaml
application:
  rate-limit:
    enabled: false
```

Or only for authentication endpoints:

```yaml
application:
  rate-limit:
    auth:
      enabled: false
```

### 6. Environment Variables

You can override values using environment variables:

```bash
export APPLICATION_RATE_LIMIT_AUTH_LIMITS_0_CAPACITY=20
export APPLICATION_RATE_LIMIT_AUTH_LIMITS_0_REFILL_TOKENS=20
export APPLICATION_RATE_LIMIT_AUTH_ENABLED=true
```

Or in Docker Compose / Kubernetes:

```yaml
environment:
  - APPLICATION_RATE_LIMIT_AUTH_LIMITS_0_CAPACITY=20
  - APPLICATION_RATE_LIMIT_AUTH_ENABLED=true
```

## Architecture and Components

### Main Components

1. **`RateLimitProperties`**: Configuration class annotated with `@ConfigurationProperties` that maps `application.yml` values.

2. **`BucketConfigurationStrategy`**: Strategy that builds Bucket4j configurations from properties.

3. **`Bucket4jRateLimiter`**: Rate limiter implementation using Bucket4j. Supports two strategies:
   - `RateLimitStrategy.AUTH`: For authentication endpoints
   - `RateLimitStrategy.BUSINESS`: For business endpoints

4. **`RateLimitingFilter`**: WebFlux filter that intercepts requests to authentication endpoints and applies IP-based rate limiting.

5. **`RateLimitingService`**: Application service that orchestrates rate limiting and publishes events.

### Execution Flow

```text
Request → RateLimitingFilter → RateLimitingService → Bucket4jRateLimiter
                ↓                                              ↓
         Identify IP                            Use BucketConfigurationStrategy
                ↓                                              ↓
    Apply AUTH strategy                       Create/Get bucket from cache
                ↓                                              ↓
         Consume token                              Return result
                ↓                                              ↓
    Allowed → continue                         Denied → 429 Too Many Requests
    Denied → 429 response
```

## Security Recommendations

### For Authentication Endpoints

1. **Multiple limits**: Use short-term (per-minute) and long-term (per-hour) limits.
2. **Strict limits**: Defaults (10/min, 100/hour) are a reasonable starting point.
3. **Identify by IP**: The system uses IP for authentication endpoints (API keys are not typically present in auth requests).

### For Business Endpoints

1. **Plan-based limits**: Adjust limits according to customer plans.
2. **Clear communication**: Ensure clients understand their API quotas.
3. **Informative headers**: The system returns `X-Rate-Limit-Remaining` and `X-Rate-Limit-Retry-After-Seconds`.

## Testing

### Manual cURL Test

```bash
# Test authentication limit (should fail after 10 requests)
for i in {1..15}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test","password":"test"}' \
    -w "\nHTTP Status: %{http_code}\n"
done
```

### Verify Rate Limit Headers

```bash
curl -v http://localhost:8080/api/auth/login
# Look for:
# < X-Rate-Limit-Remaining: 9
# < X-Rate-Limit-Retry-After-Seconds: 60
```

## Troubleshooting

### Rate limiting not working

1. Verify it's enabled: `application.rate-limit.enabled=true`
2. Verify the endpoint is listed under `application.rate-limit.auth.endpoints`
3. Check logs for messages from `RateLimitingFilter` and `BucketConfigurationStrategy`

### Startup errors

If you see errors related to `BucketConfigurationStrategy` or `RateLimitProperties`:

1. Validate the syntax of your `application.yml`
2. Make sure durations use valid suffixes (`s`, `m`, `h`, etc.)
3. Ensure all required fields are present

### Limits not behaving as expected

1. Buckets are cached by identifier. If you change configuration, restart the application.
2. Verify you are testing with the same identifier (IP).
3. For tests, use very low limits (e.g., `capacity: 2`, `refill-duration: 1m`).

## References

- [Bucket4j Documentation](https://github.com/bucket4j/bucket4j)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [OWASP Rate-Limiting](https://owasp.org/www-community/controls/Blocking_Brute_Force_Attacks)
