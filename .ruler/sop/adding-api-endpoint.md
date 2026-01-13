# SOP: Adding API Endpoints

> Step-by-step procedure for creating new REST API endpoints following Clean Architecture.

## Prerequisites

- Understand the business requirement
- Identify which domain the endpoint belongs to
- Review existing endpoints for patterns and conventions

---

## Procedure

### 1. Define the Domain Model (If New)

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/domain/`

```kotlin
// domain/UserPreference.kt
data class UserPreference(
    val id: UserPreferenceId,
    val userId: UserId,
    val theme: Theme,
    val createdAt: Instant,
    val updatedAt: Instant
)

// domain/UserPreferenceId.kt
@JvmInline
value class UserPreferenceId(val value: UUID) {
    companion object {
        fun generate(): UserPreferenceId = UserPreferenceId(UUID.randomUUID())
    }
}

// domain/Theme.kt
enum class Theme {
    LIGHT, DARK, SYSTEM
}
```

### 2. Define Repository Interface

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/domain/`

```kotlin
// domain/UserPreferenceRepository.kt
interface UserPreferenceRepository {
    suspend fun save(preference: UserPreference): UserPreference
    suspend fun findByUserId(userId: UserId): UserPreference?
}
```

### 3. Create Command/Query and Handler

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/application/`

#### For Writes (Command)

```kotlin
// application/update/UpdateUserPreferenceCommand.kt
data class UpdateUserPreferenceCommand(
    val userId: UserId,
    val theme: Theme
)

// application/update/UpdateUserPreferenceCommandHandler.kt
@Component
class UpdateUserPreferenceCommandHandler(
    private val preferenceUpdater: UserPreferenceUpdater
) {
    suspend fun handle(command: UpdateUserPreferenceCommand): UserPreference {
        return preferenceUpdater.update(command.userId, command.theme)
    }
}

// application/update/UserPreferenceUpdater.kt
@Component
class UserPreferenceUpdater(
    private val repository: UserPreferenceRepository
) {
    suspend fun update(userId: UserId, theme: Theme): UserPreference {
        val existing = repository.findByUserId(userId)
            ?: throw UserPreferenceNotFoundException(userId)

        val updated = existing.copy(
            theme = theme,
            updatedAt = Instant.now()
        )

        return repository.save(updated)
    }
}
```

#### For Reads (Query)

```kotlin
// application/find/FindUserPreferenceQuery.kt
data class FindUserPreferenceQuery(
    val userId: UserId
)

// application/find/FindUserPreferenceQueryHandler.kt
@Component
class FindUserPreferenceQueryHandler(
    private val preferenceFinder: UserPreferenceFinder
) {
    suspend fun handle(query: FindUserPreferenceQuery): UserPreference? {
        return preferenceFinder.findByUserId(query.userId)
    }
}

// application/find/UserPreferenceFinder.kt
@Component
class UserPreferenceFinder(
    private val repository: UserPreferenceRepository
) {
    suspend fun findByUserId(userId: UserId): UserPreference? {
        return repository.findByUserId(userId)
    }
}
```

### 4. Create Controller

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/infrastructure/http/`

```kotlin
// infrastructure/http/UserPreferenceController.kt
@RestController
@RequestMapping("/api/v1/user-preferences")
class UserPreferenceController(
    private val findHandler: FindUserPreferenceQueryHandler,
    private val updateHandler: UpdateUserPreferenceCommandHandler
) {

    @GetMapping("/me")
    suspend fun getMyPreferences(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserPreferenceResponse> {
        val userId = UserId(UUID.fromString(jwt.subject))
        val query = FindUserPreferenceQuery(userId)

        val preference = findHandler.handle(query)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(preference.toResponse())
    }

    @PutMapping("/me")
    suspend fun updateMyPreferences(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: UpdateUserPreferenceRequest
    ): ResponseEntity<UserPreferenceResponse> {
        val userId = UserId(UUID.fromString(jwt.subject))
        val command = UpdateUserPreferenceCommand(
            userId = userId,
            theme = request.theme
        )

        val updated = updateHandler.handle(command)
        return ResponseEntity.ok(updated.toResponse())
    }
}
```

### 5. Create Request/Response DTOs

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/infrastructure/http/`

```kotlin
// infrastructure/http/request/UpdateUserPreferenceRequest.kt
data class UpdateUserPreferenceRequest(
    @field:NotNull
    val theme: Theme
)

// infrastructure/http/response/UserPreferenceResponse.kt
data class UserPreferenceResponse(
    val id: UUID,
    val userId: UUID,
    val theme: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

// Extension function for mapping
fun UserPreference.toResponse() = UserPreferenceResponse(
    id = id.value,
    userId = userId.value,
    theme = theme.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
```

### 6. Implement Repository

Location: `server/engine/src/main/kotlin/com/cvix/{feature}/infrastructure/persistence/`

```kotlin
// infrastructure/persistence/UserPreferenceR2dbcRepository.kt
interface UserPreferenceR2dbcRepository : R2dbcRepository<UserPreferenceEntity, UUID> {
    suspend fun findByUserId(userId: UUID): UserPreferenceEntity?
}

// infrastructure/persistence/UserPreferenceStoreR2DbcRepository.kt
@Repository
class UserPreferenceStoreR2DbcRepository(
    private val r2dbcRepository: UserPreferenceR2dbcRepository
) : UserPreferenceRepository {

    override suspend fun save(preference: UserPreference): UserPreference {
        val entity = preference.toEntity()
        val saved = r2dbcRepository.save(entity).awaitSingle()
        return saved.toDomain()
    }

    override suspend fun findByUserId(userId: UserId): UserPreference? {
        return r2dbcRepository.findByUserId(userId.value)?.toDomain()
    }
}
```

### 7. Add OpenAPI Documentation

```kotlin
@Operation(
    summary = "Get user preferences",
    description = "Retrieves the authenticated user's preferences"
)
@ApiResponses(
    ApiResponse(responseCode = "200", description = "Preferences found"),
    ApiResponse(responseCode = "404", description = "Preferences not found")
)
@GetMapping("/me")
suspend fun getMyPreferences(...): ResponseEntity<UserPreferenceResponse>
```

### 8. Write Tests

#### Unit Test (Service)

```kotlin
@UnitTest
class UserPreferenceUpdaterTest {

    private val repository: UserPreferenceRepository = mockk()
    private val updater = UserPreferenceUpdater(repository)

    @Test
    fun `should update theme when preference exists`() = runTest {
        // Arrange
        val userId = UserId(UUID.randomUUID())
        val existing = createPreference(userId, Theme.LIGHT)
        coEvery { repository.findByUserId(userId) } returns existing
        coEvery { repository.save(any()) } answers { firstArg() }

        // Act
        val result = updater.update(userId, Theme.DARK)

        // Assert
        result.theme shouldBe Theme.DARK
    }
}
```

#### Integration Test (Controller)

```kotlin
@WebFluxTest(UserPreferenceController::class)
class UserPreferenceControllerTest : ControllerIntegrationTest() {

    @MockkBean
    private lateinit var findHandler: FindUserPreferenceQueryHandler

    @Test
    fun `should return preferences for authenticated user`() {
        // Arrange
        val preference = createPreference()
        coEvery { findHandler.handle(any()) } returns preference

        // Act & Assert
        webTestClient
            .mutateWith(mockJwt())
            .get()
            .uri("/api/v1/user-preferences/me")
            .exchange()
            .expectStatus().isOk
            .expectBody<UserPreferenceResponse>()
    }
}
```

---

## Checklist

- [ ] Domain model defined with value objects
- [ ] Repository interface in domain layer
- [ ] Command/Query with handler in application layer
- [ ] Service with business logic in application layer
- [ ] Controller in infrastructure layer
- [ ] Request/Response DTOs defined
- [ ] Repository implementation with R2DBC
- [ ] OpenAPI documentation added
- [ ] Unit tests for services
- [ ] Integration tests for controller
- [ ] Error handling implemented
- [ ] Security (authentication/authorization) applied

---

## Directory Structure Summary

```text
📁{feature}
├── 📁domain
│   ├── UserPreference.kt
│   ├── UserPreferenceId.kt
│   ├── Theme.kt
│   └── UserPreferenceRepository.kt
├── 📁application
│   ├── 📁find
│   │   ├── FindUserPreferenceQuery.kt
│   │   ├── FindUserPreferenceQueryHandler.kt
│   │   └── UserPreferenceFinder.kt
│   └── 📁update
│       ├── UpdateUserPreferenceCommand.kt
│       ├── UpdateUserPreferenceCommandHandler.kt
│       └── UserPreferenceUpdater.kt
└── 📁infrastructure
    ├── 📁http
    │   ├── UserPreferenceController.kt
    │   ├── 📁request
    │   │   └── UpdateUserPreferenceRequest.kt
    │   └── 📁response
    │       └── UserPreferenceResponse.kt
    └── 📁persistence
        ├── UserPreferenceEntity.kt
        ├── UserPreferenceMapper.kt
        ├── UserPreferenceR2dbcRepository.kt
        └── UserPreferenceStoreR2DbcRepository.kt
```
