# Spring Boot & WebFlux Conventions

> Conventions for backend development using Spring Boot, WebFlux, and Kotlin.

## REST API Design

- Use media type-based versioning (e.g., `application/vnd.api.v1+json`)
- Endpoints must follow RESTful principles
- Always return `ResponseEntity<T>` to control status codes and headers
- Document all endpoints using Swagger/OpenAPI annotations

## HTTP Controllers

- Annotate controllers with `@RestController`
- Keep controllers thin—business logic belongs in the service layer
- Use data classes for request/response models (DTOs)
- Validate inputs using `@Valid` and `@Validated`

## Reactive Programming (WebFlux)

- Use `Mono<T>` and `Flux<T>` consistently for all I/O-bound operations
- **Never block the reactive pipeline**—avoid `block()` at all costs in application code
- Use standard reactive operators: `flatMap`, `map`, `switchIfEmpty`

## Error Handling

- Implement a global `@ControllerAdvice` with `@ExceptionHandler` methods for uniform error
  responses
- Return a consistent, meaningful error model (e.g., `ApiError` with code and message)
- Never expose internal exceptions or stack traces to the client

### HTTP Status Codes

| Code                        | Usage                                     |
|-----------------------------|-------------------------------------------|
| `400 Bad Request`           | Invalid input or validation errors        |
| `401 Unauthorized`          | Missing or invalid authentication         |
| `403 Forbidden`             | Authenticated but not authorized          |
| `404 Not Found`             | Resource does not exist                   |
| `409 Conflict`              | State conflict (e.g., duplicate resource) |
| `422 Unprocessable Entity`  | Semantic validation errors                |
| `500 Internal Server Error` | Unexpected server errors                  |

### Error Response Format

```json
{
    "type": "https://example.com/problems/validation-error",
    "title": "Validation Error",
    "status": 400,
    "detail": "Invalid input data",
    "instance": "/api/users",
    "errors": [
        {
            "field": "email",
            "message": "Email format is invalid"
        }
    ]
}
```

## Persistence Layer (Spring Data R2DBC)

- Use Spring Data R2DBC for non-blocking database access with PostgreSQL
- Repository interfaces should extend `ReactiveCrudRepository` or `R2dbcRepository`
- Use UUID as the primary key type for all entities
- Manage database schema changes with Liquibase migrations in `src/main/resources/db/changelog`
- **Never expose persistence entities directly through the API**—always map them to DTOs

## Security

- Secure all reactive endpoints using `SecurityWebFilterChain`
- Use Keycloak for authentication and role-based access control (RBAC)
- Validate authorization on the backend—never trust role claims from the frontend alone

## Configuration Management

- Use `@ConfigurationProperties` for type-safe configuration binding instead of multiple `@Value`
  annotations
- Organize configuration classes by feature or domain (e.g., `DatabaseProperties`,
  `SecurityProperties`)
- Use Spring Profiles (`dev`, `test`, `staging`, `prod`) to manage environment-specific
  configuration
- Externalize all environment-specific values using environment variables or configuration files
- **Never commit sensitive configuration** (passwords, tokens) to version control

## Dependency Injection

- **Always use constructor injection**—it makes dependencies explicit and improves testability
- Avoid field injection with `@Autowired`—it hides dependencies and makes testing harder
- Mark optional dependencies explicitly using Kotlin's nullable types or `Optional<T>`
- Use `@Lazy` annotation sparingly and only when necessary to break circular dependencies

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    // Business logic
}
```

## Bean Lifecycle

- Use `@PostConstruct` for initialization logic that depends on injected dependencies
- Use `@PreDestroy` for cleanup logic (closing connections, releasing resources)
- Avoid heavy initialization in constructors—defer to `@PostConstruct` when possible
- Be mindful of bean initialization order when using `@DependsOn`

## Transaction Management

- **Note:** Traditional `@Transactional` does not work with R2DBC and reactive flows
- Use `TransactionalOperator` for programmatic transaction management in reactive contexts
- Keep transactions short and focused—avoid long-running operations inside transactions
- Handle transaction rollback explicitly using reactive operators like `onErrorResume`

```kotlin
@Service
class OrderService(
    private val transactionalOperator: TransactionalOperator
) {
    fun placeOrder(order: Order): Mono<Order> {
        return orderRepository.save(order)
            .`as`(transactionalOperator::transactional)
    }
}
```

## Caching

- Use Spring's `@Cacheable`, `@CachePut`, and `@CacheEvict` annotations for declarative caching
- For reactive applications, ensure cache implementations support non-blocking operations
- Configure cache TTL and eviction policies based on data volatility
- Use descriptive cache names (e.g., `user-profiles`, `product-catalog`)

## Monitoring & Observability

- Enable Spring Boot Actuator for production-ready monitoring endpoints
- Expose only necessary Actuator endpoints in production—secure sensitive endpoints
- Implement custom health indicators for critical dependencies (database, external APIs)
- Use Micrometer for metrics collection and export to monitoring systems (Prometheus, Grafana)
- Integrate distributed tracing (e.g., OpenTelemetry, Zipkin) for request flow visibility

```kotlin
@Component
class DatabaseHealthIndicator(
    private val databaseClient: DatabaseClient
) : ReactiveHealthIndicator {
    override fun health(): Mono<Health> {
        return databaseClient.sql("SELECT 1").fetch().one()
            .map { Health.up().build() }
            .onErrorResume { Mono.just(Health.down(it).build()) }
    }
}
```

## CORS Configuration

- Configure CORS explicitly for WebFlux using `CorsWebFilter` or `@CrossOrigin` annotations
- Define allowed origins, methods, and headers based on security requirements
- **Avoid** using `allowedOrigins("*")` in production—specify exact origins
- Set `allowCredentials(true)` only when necessary and with specific origins

```kotlin
@Configuration
class CorsConfig {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("https://app.example.com")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        return CorsWebFilter(source)
    }
}
```

## Rate Limiting

- Implement rate limiting for public APIs to prevent abuse
- Use token bucket or sliding window algorithms compatible with reactive flows
- Return `429 Too Many Requests` with `Retry-After` header when rate limit is exceeded
- Consider per-user and per-IP rate limiting strategies

## Request Validation

- Use Bean Validation (`jakarta.validation`) annotations on DTOs: `@NotNull`, `@Size`, `@Email`,
  `@Pattern`
- Create custom validators for complex business rules using `@Constraint` and `ConstraintValidator`
- Use validation groups to apply different rules for different scenarios (create vs. update)
- Return all validation errors in a single response, not just the first error

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueEmailValidator::class])
annotation class UniqueEmail(
    val message: String = "Email already exists",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
```

## Logging

- Use SLF4J with Logback as the logging framework
- Configure log levels per environment:
    - `dev`: `DEBUG` for application code, `INFO` for libraries
    - `prod`: `INFO` for application code, `WARN` for libraries
- Use structured logging (JSON format) in production for better log analysis
- Include correlation IDs in logs for request tracing
- **Never log sensitive data** (passwords, tokens, PII)

| Level   | Usage                                                                |
|---------|----------------------------------------------------------------------|
| `ERROR` | Critical failures requiring immediate attention                      |
| `WARN`  | Recoverable issues or degraded functionality                         |
| `INFO`  | Significant application events (startup, shutdown, major operations) |
| `DEBUG` | Detailed diagnostic information for troubleshooting                  |

## Application Profiles

| Profile   | Purpose                                                                   |
|-----------|---------------------------------------------------------------------------|
| `dev`     | Local development with mocks, verbose logging, hot reload                 |
| `test`    | Automated testing with embedded databases and mocks                       |
| `staging` | Production-like environment for final validation                          |
| `prod`    | Production with optimized settings, external services, security hardening |

- Activate profiles via `SPRING_PROFILES_ACTIVE` environment variable or `--spring.profiles.active`
- Use `@Profile` annotation to conditionally load beans based on active profiles
- Document profile-specific behavior and configuration
