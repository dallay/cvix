# User Context in Logs with MDC (Mapped Diagnostic Context)

This implementation provides automatic propagation of user context in application logs, enabling
traceability without exposing sensitive information.

## Implemented Components

### 1. LogMasker (`LogMasker.kt`)

Utility for masking sensitive information using SHA-256:

- Converts IDs (userId, workspaceId) into 12-character hashes
- Not reversible, but consistent for the same ID
- Enables traceability without exposing real data

### 2. UserMdcCoFilter (`UserMdcCoFilter.kt`)

Coroutine-based filter (`CoWebFilter`) that:

- Extracts userId from the JWT token in the security context
        - Extracts workspaceId from (in order of precedence):
                1. Header `X-Workspace-Id`
                2. Query parameter `workspaceId`
                3. Request body (JSON, for POST/PUT/PATCH)
            The filter always prefers the header first, then query, and finally the body if neither is present.
- Masks IDs using `LogMasker`
- Propagates context via:
  - **MDCContext**: For coroutine-based code (services, handlers)
  - **ReactorContext**: For reactive library calls (WebClient, R2DBC)

### 3. Dependency: kotlinx-coroutines-slf4j

Added in `gradle/libs.versions.toml` and `engine.gradle.kts`:

```kotlin
implementation(libs.kotlinx.coroutines.slf4j)
```

## How It Works

### Context Propagation Flow

```text
1. HTTP Request → UserMdcCoFilter
2. Extracts userId from JWT
3. Extracts workspaceId from header, query param, or body
4. Masks: "c4af4e2f-b432-4c3b-8405-cca86cd5b97b" → "a7d2f8e9c1b3"
5. Propagates via MDCContext + ReactorContext
6. Suspended code accesses the context
7. Logs automatically include the masked ID
```

### Example Usage in Services

```kotlin
suspend fun updateResume(id: UUID, userId: UUID) {
    // MDC already contains maskedUserId from the filter
    log.info("Updating resume")
    // Log will show: "Updating resume [user=a7d2f8e9c1b3]"

    // Calls to reactive libraries maintain the context
    val data = repository.findById(id).awaitSingle()

    log.info("Resume updated successfully")
    // maskedUserId persists throughout execution
}
```

## Logback Configuration

To display user context in logs, update `logback-spring.xml`:

```xml

<pattern>
    %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - [user=%X{maskedUserId}]
    [ws=%X{maskedWorkspaceId}] - %msg%n
</pattern>
```

Currently, the pattern is simplified (without MDC visible) to avoid affecting existing tests. MDC is
available and working, but not shown in the default output.

## Security Features

1. **Not Reversible**: SHA-256 hashes cannot be converted back to the original IDs
2. **Consistent**: The same ID always produces the same hash (useful for log correlation)
3. **Compact**: 12 characters instead of the full 36-character UUID
4. **Automatic**: No need to modify existing code
5. **Disabled in Tests**: The filter uses `@Profile("!test")` to avoid interfering with tests

## Advantages Over Previous Approach

### Before (Direct Logging of IDs)

```kotlin
log.info("Updating resume - id={}, userId={}", id, userId)
// Log: "Updating resume - id=123, userId=c4af4e2f-b432-4c3b-8405-cca86cd5b97b"
// ❌ Exposes PII
// ❌ Requires modifying every log
// ❌ Inconsistent
```

### Now (MDC with Masked IDs)

```kotlin
log.info("Updating resume - id={}", id)
// Log: "[user=a7d2f8e9c1b3] Updating resume - id=123"
// ✅ No PII exposure
// ✅ Automatic propagation
// ✅ Consistent throughout the app
// ✅ Works with coroutines and reactive libraries
```

## MDC Keys

- `maskedUserId`: Hash of the userId extracted from JWT
- `maskedWorkspaceId`: Hash of the workspaceId extracted from header, query param, or body

## Compatibility with Coroutines and Reactor

This implementation is hybrid and supports:

1. **Coroutine-based code**: Services, handlers, use cases
    - MDCContext travels with the coroutine
    - Maintained even if the coroutine suspends and resumes on another thread

2. **Reactive libraries**: WebClient, R2DBC
    - ReactorContext makes the context available to `Mono/Flux`
    - Logs inside reactive operators also have access to the context

## Testing

The filter is disabled in tests (`@Profile("!test")`) to avoid interference. If you need to test MDC
functionality, enable the appropriate profile or mock the filter.

## Optional Next Steps

1. **Enable MDC in Logback**: Update the pattern to show `maskedUserId` and `maskedWorkspaceId`
2. **Extend context**: Add more fields (requestId, correlationId, etc.)
3. **Observability integration**: Propagate context to Zipkin/Jaeger for distributed tracing
4. **User-based metrics**: Use maskedUserId to group metrics without exposing PII

## References

- [kotlinx-coroutines-slf4j](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/)
- [SLF4J MDC](https://www.slf4j.org/manual.html#mdc)
- [Spring WebFlux CoWebFilter](https://docs.spring.io/spring-framework/reference/web/webflux/reactive-spring.html)
