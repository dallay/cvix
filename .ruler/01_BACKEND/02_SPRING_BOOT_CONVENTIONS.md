# Spring Boot & WebFlux Conventions

> This document defines the conventions for backend development using Spring Boot, WebFlux, and Kotlin.

## REST API Design

- Use media type-based versioning (e.g., `application/vnd.api.v1+json`).
- Endpoints must follow RESTful principles.
- Always return `ResponseEntity<T>` to control status codes and headers.
- Document all endpoints using Swagger/OpenAPI annotations.

## HTTP Controllers

- Annotate controllers with `@RestController`.
- Keep controllers thin. Business logic belongs in the service layer.
- Use data classes for request/response models (DTOs).
- Validate inputs using `@Valid` and `@Validated`.

## Reactive Programming (WebFlux)

- Use `Mono<T>` and `Flux<T>` consistently for all I/O-bound operations.
- **Never block the reactive pipeline**. Avoid `block()` at all costs in application code.
- Use standard reactive operators like `flatMap`, `map`, and `switchIfEmpty`.

## Error Handling

- Implement a global `@ControllerAdvice` with `@ExceptionHandler` methods for uniform error responses.
- Return a consistent, meaningful error model (e.g., `ApiError` with a code and message).
- Never expose internal exceptions or stack traces to the client.

## Persistence Layer (Spring Data R2DBC)

- Use Spring Data R2DBC for non-blocking database access with PostgreSQL.
- Repository interfaces should extend `ReactiveCrudRepository` or `R2dbcRepository`.
- Use UUID as the primary key type for all entities.
- Manage database schema changes with Liquibase migrations, located in `src/main/resources/db/changelog`.
- Never expose persistence entities directly through the API. Always map them to DTOs.

## Security

- Secure all reactive endpoints using `SecurityWebFilterChain`.
- Use Keycloak for authentication and role-based access control (RBAC).
- Validate authorization on the backend. Never trust role claims from the frontend alone.

## Testing

- Use `@WebFluxTest` for controller tests and `@DataR2dbcTest` for repository tests.
- Use Testcontainers for integration tests that require a real database or other services.
- Use `WebTestClient` for testing reactive endpoints.

## Configuration Management

- Use `@ConfigurationProperties` for type-safe configuration binding instead of multiple `@Value` annotations.
- Organize configuration classes by feature or domain (e.g., `DatabaseProperties`, `SecurityProperties`).
- Use Spring Profiles (`dev`, `test`, `staging`, `prod`) to manage environment-specific configuration.
- Externalize all environment-specific values (database URLs, API keys, etc.) using environment variables or configuration files.
- Never commit sensitive configuration (passwords, tokens) to version control. Use environment variables or secret management tools.

## Dependency Injection

- **Always use constructor injection**. It makes dependencies explicit and improves testability.
- Avoid field injection with `@Autowired`. It hides dependencies and makes testing harder.
- Mark optional dependencies explicitly using Kotlin's nullable types or `Optional<T>`.
- Use `@Lazy` annotation sparingly and only when necessary to break circular dependencies.

Example:

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

- Use `@PostConstruct` for initialization logic that depends on injected dependencies.
- Use `@PreDestroy` for cleanup logic (closing connections, releasing resources).
- Avoid heavy initialization in constructors. Defer to `@PostConstruct` when possible.
- Be mindful of bean initialization order when using `@DependsOn`.

## Transaction Management

- **Note:** Traditional `@Transactional` does not work with R2DBC and reactive flows.
- Use `TransactionalOperator` for programmatic transaction management in reactive contexts.
- Keep transactions short and focused. Avoid long-running operations inside transactions.
- Handle transaction rollback explicitly using reactive operators like `onErrorResume`.

Example:

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

- Use Spring's `@Cacheable`, `@CachePut`, and `@CacheEvict` annotations for declarative caching.
- For reactive applications, ensure cache implementations support non-blocking operations.
- Configure cache TTL and eviction policies based on data volatility.
- Use cache names that clearly describe the cached data (e.g., `user-profiles`, `product-catalog`).

## Monitoring & Observability

- Enable Spring Boot Actuator for production-ready monitoring endpoints.
- Expose only necessary Actuator endpoints in production. Secure sensitive endpoints (e.g., `/actuator/env`, `/actuator/metrics`).
- Implement custom health indicators for critical dependencies (database, external APIs).
- Use Micrometer for metrics collection and export to monitoring systems (Prometheus, Grafana).
- Integrate distributed tracing (e.g., OpenTelemetry, Zipkin) for request flow visibility.

Example health check:

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

## Error Handling Details

- Use consistent HTTP status codes:
  - `400 Bad Request`: Invalid input or validation errors
  - `401 Unauthorized`: Missing or invalid authentication
  - `403 Forbidden`: Authenticated but not authorized
  - `404 Not Found`: Resource does not exist
  - `409 Conflict`: State conflict (e.g., duplicate resource)
  - `422 Unprocessable Entity`: Semantic validation errors
  - `500 Internal Server Error`: Unexpected server errors
- Return structured error responses with machine-readable error codes:

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


## Database Migrations (Liquibase)

- **Standard structure:**

```text
📁db
 └── 📁changelog
   ├── 📁data
   │   ├── authority.csv
   │   ├── federated_identities_dev.csv
   │   ├── user_authority_dev.csv
   │   ├── users_dev.csv
   │   ├── workspace_members_dev.csv
   │   ├── workspaces_dev.csv
   ├── 📁migrations
   │   ├── 001-initial-schema.yaml
   │   ├── 002-workspaces.yaml
   │   ├── 002a-workspaces-triggers.yaml
   │   ├── 002b-workspaces-rls.yaml
   │   ├── 002c-workspaces-default-constraint.yaml
   │   ├── 002d-sessions-table.yaml
   │   ├── 002e-authentication-events-table.yaml
   │   ├── 002f-federated-identities-table.yaml
   │   ├── 003-session-optimization.yaml
   │   ├── 004-resumes.yaml
   │   ├── 99900001-data-dev-test-users.yaml
   ├── master.yaml
   └── README.md
```

- The `master.yaml` file includes all changes and data in execution order.
- Changes are organized by number and topic, using suffixes for variants (e.g., triggers, rls, constraints).
- Development and test data are located in `changelog/data/` and are loaded only in non-production environments.
- Never modify a migration file that has already been applied in production; always create a new file for each change.
- Use YAML files for migrations and CSV files for bulk data.
- Document each relevant migration in the `README.md` inside `changelog`.
- Test migrations on a clean database and in staging environments before production.
- Keep changes small, atomic, and use descriptive names.

## CORS Configuration

- Configure CORS explicitly for WebFlux using `CorsWebFilter` or `@CrossOrigin` annotations.
- Define allowed origins, methods, and headers based on security requirements.
- Avoid using `allowedOrigins("*")` in production. Specify exact origins.
- Set `allowCredentials(true)` only when necessary and with specific origins.

Example:

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

- Implement rate limiting for public APIs to prevent abuse.
- Use token bucket or sliding window algorithms compatible with reactive flows.
- Return `429 Too Many Requests` with `Retry-After` header when rate limit is exceeded.
- Consider per-user and per-IP rate limiting strategies.

## API Pagination

- Use query parameters for pagination: `page` (0-indexed), `size`, `sort`.
- Return pagination metadata in responses:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

- Support cursor-based pagination for large datasets or real-time feeds.
- Document pagination parameters in OpenAPI/Swagger.

## Request Validation

- Use Bean Validation (`jakarta.validation`) annotations on DTOs: `@NotNull`, `@Size`, `@Email`, `@Pattern`.
- Create custom validators for complex business rules using `@Constraint` and `ConstraintValidator`.
- Use validation groups to apply different validation rules for different scenarios (create vs. update).
- Return all validation errors in a single response, not just the first error.

Example custom validator:

```kotlin
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

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

- Use SLF4J with Logback as the logging framework.
- Configure log levels per environment:
  - `dev`: `DEBUG` for application code, `INFO` for libraries
  - `prod`: `INFO` for application code, `WARN` for libraries
- Use structured logging (JSON format) in production for better log analysis.
- Include correlation IDs in logs for request tracing.
- Avoid logging sensitive data (passwords, tokens, PII).
- Log at appropriate levels:
  - `ERROR`: Critical failures requiring immediate attention
  - `WARN`: Recoverable issues or degraded functionality
  - `INFO`: Significant application events (startup, shutdown, major operations)
  - `DEBUG`: Detailed diagnostic information for troubleshooting

Example:

```kotlin
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

logger.info { "Creating user with email: ${user.email}" }
logger.error(exception) { "Failed to process payment" }
```

## Application Profiles

- Use Spring Profiles to manage environment-specific behavior:
  - `dev`: Local development with mocks, verbose logging, hot reload
  - `test`: Automated testing with embedded databases and mocks
  - `staging`: Production-like environment for final validation
  - `prod`: Production with optimized settings, external services, security hardening
- Activate profiles via `SPRING_PROFILES_ACTIVE` environment variable or `--spring.profiles.active` command-line argument.
- Use `@Profile` annotation to conditionally load beans based on active profiles.
- Document profile-specific behavior and configuration in README or documentation.
