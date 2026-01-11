---
name: kotlin
description: >
  Kotlin conventions with null safety, coroutines, and functional patterns.
  Trigger: When working with .kt files, coroutines, or Kotlin-specific patterns.
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
metadata:
  author: cvix
  version: "1.0"
---

# Kotlin Skill

Conventions for writing idiomatic, safe, and maintainable Kotlin code.

## When to Use

- Creating or modifying `.kt` files
- Working with coroutines and Flow
- Implementing domain models and services
- Writing unit tests with Kotest

## Critical Patterns

### 1. Null Safety - NEVER Use `!!`

**STRICTLY AVOID the `!!` operator**. Use safe alternatives:

```kotlin
// ❌ NEVER do this
val name = user!!.name

// ✅ Safe call with elvis
val name = user?.name ?: "Unknown"

// ✅ requireNotNull for asserting
val name = requireNotNull(user?.name) { "User name is required" }

// ✅ let for scoped operations
user?.let {
    sendEmail(it.email)
}

// ✅ takeIf/takeUnless for conditional
val activeUser = user.takeIf { it.isActive }
```

### 2. Data Classes for Models

**ALWAYS use data classes for immutable models**:

```kotlin
data class User(
    val id: UserId,
    val email: Email,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
)

// ✅ Value classes for type safety
@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "Invalid email format" }
    }
}
```

### 3. Sealed Classes for State

**Use sealed classes for restricted hierarchies**:

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: DomainError) : Result<Nothing>
    data object Loading : Result<Nothing>
}

// Usage with when (exhaustive)
fun handleResult(result: Result<User>) = when (result) {
    is Result.Success -> displayUser(result.data)
    is Result.Failure -> showError(result.error)
    Result.Loading -> showSpinner()
}

// Domain errors
sealed interface DomainError {
    data class NotFound(val id: String) : DomainError
    data class Validation(val field: String, val message: String) : DomainError
    data object Unauthorized : DomainError
}
```

### 4. Error Handling with Result

**Prefer `Result<T>` over exceptions for business logic**:

```kotlin
// ✅ Return Result for operations that can fail
fun findUser(id: UserId): Result<User> = runCatching {
    userRepository.findById(id)
        ?: throw NotFoundException("User not found")
}

// ✅ Chain operations
fun processUser(id: UserId): Result<ProcessedUser> {
    return findUser(id)
        .mapCatching { user -> validate(user) }
        .mapCatching { validated -> enrich(validated) }
        .onFailure { logger.error(it) { "Failed to process user $id" } }
}

// ✅ Handle result
findUser(userId).fold(
    onSuccess = { user -> Response.ok(user) },
    onFailure = { error -> Response.error(error.message) },
)
```

### 5. Coroutines and Flow

**Embrace structured concurrency**:

```kotlin
// ✅ Suspend functions for async operations
suspend fun fetchUser(id: UserId): User {
    return withContext(Dispatchers.IO) {
        userRepository.findById(id)
    }
}

// ✅ Flow for streams
fun observeUsers(): Flow<List<User>> = flow {
    while (true) {
        emit(userRepository.findAll())
        delay(5.seconds)
    }
}.flowOn(Dispatchers.IO)

// ✅ Proper scope management
class UserService(
    private val scope: CoroutineScope,
) {
    fun startSync() {
        scope.launch {
            observeUsers().collect { users ->
                processUsers(users)
            }
        }
    }
}
```

### 6. Extension Functions

**Use for enhancing existing types**:

```kotlin
// ✅ Domain-specific extensions
fun String.toSlug(): String =
    lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), "-")

fun Instant.isRecent(threshold: Duration = 24.hours): Boolean =
    this.isAfter(Instant.now().minus(threshold))

// ✅ Null-safe extensions
fun String?.orEmpty(): String = this ?: ""
fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()
```

## Naming Conventions

| Element             | Convention                | Example                                 |
|---------------------|---------------------------|-----------------------------------------|
| Classes/Interfaces  | `PascalCase`              | `UserService`, `WorkspaceRepository`    |
| Functions/Variables | `camelCase`               | `findById`, `userName`                  |
| Constants           | `UPPER_SNAKE_CASE`        | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`    |
| Test Methods        | Backticks                 | `` `should return user when exists` ``  |
| Booleans            | `is`, `has`, `are` prefix | `isActive`, `hasPermission`, `areValid` |

## Code Style

```kotlin
// ✅ Expression body for simple functions
fun double(x: Int): Int = x * 2

// ✅ Trailing commas (helps with diffs)
data class Config(
    val host: String,
    val port: Int,
    val timeout: Duration,  // ← trailing comma
)

// ✅ Named arguments for 3+ parameters
createUser(
    name = "John",
    email = "john@example.com",
    role = Role.USER,
)

// ✅ Prefer val over var
val items = mutableListOf<Item>()  // val for reference, mutable for content
```

## Logging

```kotlin
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class UserService {
    fun processUser(user: User) {
        logger.info { "Processing user: ${user.id}" }

        try {
            // logic
        } catch (e: Exception) {
            logger.error(e) { "Failed to process user: ${user.id}" }
            throw e
        }
    }
}

// ❌ NEVER log sensitive data
logger.info { "User logged in: password=${user.password}" }  // WRONG!
```

## Anti-Patterns

❌ **`!!` operator** - Use `?.`, `?:`, `requireNotNull`, `let`
❌ **Catching generic `Exception`** - Catch specific exceptions
❌ **`var` when `val` works** - Prefer immutability
❌ **Inheritance over composition** - Prefer composition
❌ **Wildcard imports** - Except `java.util.*`, `io.mockk.*`
❌ **Mutable public properties** - Use private set or immutable data

## Commands

```bash
# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.cvix.user.UserServiceTest"

# Run tests by tag
./gradlew test -PincludeTags=unit

# Lint with Detekt
./gradlew detektAll

# Build
./gradlew build
```

## Resources

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
