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
