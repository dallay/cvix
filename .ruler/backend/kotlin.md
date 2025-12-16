# Kotlin Conventions

> Standard conventions and best practices for writing Kotlin code across the entire codebase.

## General Style

- Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use 4 spaces for indentation
- Use `val` over `var` whenever possible
- Prefer expression bodies for functions when concise
- Functions with exactly one statement (the return statement) should use expression body syntax

## Null Safety

- **Strictly avoid** the `!!` operator
- Leverage Kotlin's null-safety features: `?.`, `?:`, and `requireNotNull`
- Model optional data using nullable types (`?`) or sealed classes for complex scenarios

## Functions and Expressions

- Prefer top-level functions for pure, stateless utility operations
- Use extension functions to enhance existing classes with new functionality
- Keep functions small and focused on a single responsibility

## Object-Oriented Design

- Use `data class` for immutable models that primarily hold data
- Use `sealed class` or `sealed interface` for restricted class hierarchies (result types, state machines)
- Prefer composition over inheritance

## Collections and Functional Style

- Use functional operators (`map`, `filter`, `fold`, etc.) over imperative loops where it improves readability
- Prefer immutable collections
- Use functions like `toList()` or `toMap()` to create new collections instead of modifying existing ones

## Error Handling

- Use sealed classes or `Result<T>` for handling recoverable failures instead of throwing exceptions
- Avoid catching generic `Exception`
- Use `runCatching {}` for wrapping operations that may throw exceptions

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes/Interfaces | `PascalCase` | `UserService`, `WorkspaceRepository` |
| Functions/Variables | `camelCase` | `findById`, `userName` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Test Methods | Backticks with description | `` `should return user when exists` `` |

## Coroutines

- Embrace structured concurrency
- Launch coroutines within a `CoroutineScope` (e.g., `viewModelScope`, `lifecycleScope`)
- Mark functions that perform long-running or I/O-bound work with `suspend`
- Use `Flow` for reactive streams of data

## Import Ordering

- Order imports: standard library first, then third-party, then project-specific
- Remove unused imports automatically (IDE or linter)

## Documentation

- Use KDoc for all public classes, functions, and properties, especially in shared modules
- Document non-obvious business logic and public APIs

## Logging

- Use SLF4J for logging: `logger.info { ... }`
- Prefer structured logging for important events
- **Never** log sensitive data (passwords, tokens, PII)

```kotlin
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

logger.info { "Creating user with email: ${user.email}" }
logger.error(exception) { "Failed to process payment" }
```

## Annotation Usage

- Use custom annotations for DI, validation, or configuration only when necessary
- Document custom annotations and their intended usage

## Immutability

- Prefer immutable data structures (`val`, immutable collections)
- Avoid mutable state in shared and domain modules

## Deprecation

- Mark deprecated code with `@Deprecated` annotation and provide a replacement or migration path
- Remove deprecated code after one release cycle unless otherwise justified

## Performance

- Avoid unnecessary object allocations and boxing
- Use inline functions for small, performance-critical utilities
- Profile and optimize hot paths as needed
