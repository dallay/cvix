---
name: spring-boot
description: >
  Spring Boot with WebFlux, R2DBC, and reactive patterns for Kotlin.
  Trigger: When working with controllers, services, repositories, or Spring configuration.
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
metadata:
  author: cvix
  version: "1.0"
---

# Spring Boot Skill

Conventions for Spring Boot backend development with WebFlux, R2DBC, and Kotlin.

> **Architecture Note**: This skill covers the **Infrastructure Layer** of our Hexagonal
> Architecture.
> For domain models, use cases, and overall feature organization, see
> the [hexagonal-architecture skill](.ruler/skills/hexagonal-architecture/SKILL.md).

## Layer Context

| Layer                           | What Goes Here                                 | Spring Annotations? |
| ------------------------------- | ---------------------------------------------- | ------------------- |
| **Domain**                      | Entities, Value Objects, Repository interfaces | ❌ NO                |
| **Application**                 | Commands, Queries, Handlers, Use Case services | ❌ NO                |
| **Infrastructure** (this skill) | Controllers, R2DBC repos, Configs, Adapters    | ✅ YES               |

## When to Use

- Creating REST controllers (HTTP adapters)
- Implementing repository adapters (R2DBC implementations)
- Configuring Spring Security, CORS, caching
- Wiring application services as Spring beans
- Writing integration tests with Testcontainers

## Critical Patterns

### 1. Controller Layer - Thin HTTP Adapters

**Controllers are infrastructure adapters. They ONLY handle HTTP concerns and delegate to
application layer handlers**:

```kotlin
// infrastructure/http/UserController.kt
@Validated
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
class UserController(
    private val createUserHandler: CreateUserCommandHandler,  // Application layer
    private val findUserHandler: FindUserQueryHandler,        // Application layer
) {
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve a single user's information by their unique identifier",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User found",
                content = [Content(schema = Schema(implementation = UserResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping(
        "/{id}",
        produces = ["application/vnd.api.v1+json"],
    )
    suspend fun getUser(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        logger.info("Fetching user with id: {}", id)

        return findUserHandler.handle(FindUserQuery(id))
            .map { user ->
                logger.info("Successfully retrieved user: {}", id)
                ResponseEntity.ok(user.toResponse())
            }
            .getOrElse { error ->
                when (error) {
                    is NotFoundException -> {
                        logger.warn("User not found: {}", id)
                        ResponseEntity.notFound().build()
                    }
                    else -> {
                        logger.error("Error retrieving user: {}", id, error)
                        ResponseEntity.internalServerError().build()
                    }
                }
            }
    }

    @Operation(
        summary = "Create a new user",
        description = "Register a new user in the system",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = [Content(schema = Schema(implementation = UserResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "User already exists",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PostMapping(
        produces = ["application/vnd.api.v1+json"],
        consumes = ["application/json"],
    )
    suspend fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        serverRequest: ServerHttpRequest,
    ): ResponseEntity<UserIdResponse> {
        logger.info("Creating user with email: {}", request.email)

        val command = CreateUserCommand(
            email = request.email,
            name = request.name,
            metadata = mapOf(
                "userAgent" to (serverRequest.headers.getFirst("User-Agent") ?: "unknown"),
                "ipAddress" to ClientIpExtractor.extract(serverRequest),
            ),
        )

        // Handler returns Result<UserId> - handle success/failure explicitly
        val result = createUserHandler.handle(command)

        return result.fold(
            onSuccess = { userId ->
                logger.info("Successfully created user: {}", userId)
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(UserIdResponse(userId.value))
            },
            onFailure = { error ->
                logger.error("Failed to create user: {}", error.message)
                throw error // Let @ControllerAdvice handle the exception
            },
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }
}

// infrastructure/http/request/CreateUserRequest.kt
data class CreateUserRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val name: String,
)

// infrastructure/http/response/UserResponse.kt
data class UserResponse(
    val id: UUID,
    val email: String,
    val name: String,
    val isActive: Boolean,
)

fun User.toResponse() = UserResponse(
    id = id.value,
    email = email.value,
    name = name,
    isActive = isActive,
)
```

### 2. Application Services - Wired via Configuration

**Application services contain business logic and are wired as Spring beans in the Infrastructure
layer**:

> **Note**: These services live in the `application/` layer but are instantiated via Spring
`@Configuration` classes in the infrastructure layer. They have NO Spring annotations themselves.

```kotlin
// application/UserCreator.kt (NO Spring annotations!)
class UserCreator(
    private val userRepository: UserRepository,        // Domain interface (port)
    private val emailService: EmailService,            // Domain interface (port)
) {
    suspend fun create(user: User, metadata: Map<String, String>): Result<User> = runCatching {
        logger.info { "Creating user: ${user.email} from IP: ${metadata["ipAddress"]}" }

        // Note: Email format validation is handled by the Email value object constructor.
        // Service layer focuses on business rules like uniqueness.

        // Check uniqueness (business rule)
        userRepository.findByEmail(user.email)?.let {
            logger.warn { "Duplicate email attempt: ${user.email}" }
            throw ConflictException("Email already exists")
        }

        // Persist
        val savedUser = userRepository.save(user.copy(metadata = metadata))

        logger.info { "User created successfully: ${savedUser.id}" }

        // Side effects (non-blocking)
        emailService.sendWelcome(savedUser)

        savedUser
    }

    suspend fun findById(id: UserId): Result<User> = runCatching {
        logger.debug { "Looking up user: $id" }

        userRepository.findById(id.value)
            ?.also { logger.debug { "User found: $id" } }
            ?: throw NotFoundException("User not found: $id")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

// infrastructure/configuration/UserConfiguration.kt (Spring wiring)
@Configuration
class UserConfiguration {
    @Bean
    fun userCreator(
        userRepository: UserRepository,
        emailService: EmailService,
    ) = UserCreator(userRepository, emailService)
}
```

### 3. Repository Layer - R2DBC (Infrastructure Adapter)

**Repository implementations are infrastructure adapters that implement domain ports**:

```kotlin
// domain/UserRepository.kt (PORT - in domain layer, NO Spring!)
interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findById(id: UserId): User?
    suspend fun findByEmail(email: Email): User?
}

// infrastructure/persistence/entity/UserEntity.kt
@Table("users")
data class UserEntity(
    @Id val id: UUID? = null,
    val email: String,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

// infrastructure/persistence/repository/UserR2DbcRepository.kt
interface UserR2DbcRepository : ReactiveCrudRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Mono<UserEntity>
    fun findAllByIsActive(isActive: Boolean): Flux<UserEntity>

    @Query("SELECT * FROM users WHERE created_at > :since")
    fun findRecentUsers(since: Instant): Flux<UserEntity>
}

// infrastructure/persistence/UserStoreR2DbcRepository.kt (ADAPTER)
@Repository
class UserStoreR2DbcRepository(
    private val r2dbcRepository: UserR2DbcRepository,
    private val mapper: UserMapper,
) : UserRepository {

    override suspend fun save(user: User): User {
        val entity = mapper.toEntity(user)
        val saved = r2dbcRepository.save(entity).awaitSingle()
        return mapper.toDomain(saved)
    }

    override suspend fun findById(id: UserId): User? {
        return r2dbcRepository.findById(id.value).awaitSingleOrNull()
            ?.let { mapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: Email): User? {
        return r2dbcRepository.findByEmail(email.value).awaitSingleOrNull()
            ?.let { mapper.toDomain(it) }
    }
}

// infrastructure/persistence/mapper/UserMapper.kt
@Component
class UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        id = UserId(requireNotNull(entity.id)),
        email = Email(entity.email),
        name = entity.name,
        isActive = entity.isActive,
    )

    fun toEntity(domain: User): UserEntity = UserEntity(
        id = domain.id?.value,
        email = domain.email.value,
        name = domain.name,
        isActive = domain.isActive,
    )
}
```

### 4. Error Handling - Global Exception Handler

**Consistent error responses across all endpoints**:

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ProblemDetail> {
        logger.debug { "Resource not found: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Not found"))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ProblemDetail> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflict"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val errors = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage}"
        }
        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
        ).apply {
            setProperty("errors", errors)
        }
        return ResponseEntity.badRequest().body(problem)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ProblemDetail> {
        logger.error(ex) { "Unhandled exception" }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR))
    }
}
```

### 5. Configuration - Type-Safe Properties

**ALWAYS use `@ConfigurationProperties`**:

```kotlin
@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    val jwtSecret: String,
    val jwtExpiration: Duration = Duration.ofHours(24),
    val allowedOrigins: List<String> = emptyList(),
)

@ConfigurationProperties(prefix = "app.database")
data class DatabaseProperties(
    val host: String,
    val port: Int = 5432,
    val name: String,
    val poolSize: Int = 10,
)

// Enable in main class
@SpringBootApplication
@ConfigurationPropertiesScan
class Application
```

### Transaction and Caching Guidance

**Transaction Demarcation:**

- Apply `@Transactional` at service-layer write boundaries and repository orchestration
- For reactive stacks (R2DBC), use `TransactionalOperator` instead of imperative `@Transactional`:

```kotlin
// Imperative (blocking) - use @Transactional
@Service
class UserService(private val repository: UserRepository) {
    @Transactional
    fun transferFunds(from: UserId, to: UserId, amount: Money) { ... }
}

// Reactive (R2DBC) - use TransactionalOperator
@Service
class UserService(
    private val repository: UserRepository,
    private val txOperator: TransactionalOperator,
) {
    suspend fun transferFunds(from: UserId, to: UserId, amount: Money) {
        txOperator.executeAndAwait {
            val sender = repository.findById(from)
            val receiver = repository.findById(to)
            // ... perform transfer
        }
    }
}
```

**Caching with Spring Cache:**

```kotlin
@Service
class UserService(private val repository: UserRepository) {
    @Cacheable("users", key = "#id.value")
    suspend fun findById(id: UserId): User? = repository.findById(id)

    @CacheEvict("users", key = "#user.id.value")
    suspend fun update(user: User): User = repository.save(user)

    @CachePut("users", key = "#result.id.value")
    suspend fun create(user: User): User = repository.save(user)
}
```

- Define TTL and eviction policies in cache configuration
- Use `@CacheEvict(allEntries = true)` for bulk invalidation

**R2DBC Connection Pooling:**

```yaml
# application.yml
spring:
  r2dbc:
    pool:
      initial-size: 5
      max-size: 20           # Align with expected service concurrency
      max-idle-time: 30m
      validation-query: SELECT 1
```

> **Tip**: Set `max-size` based on your service's concurrent request capacity.
> For CPU-bound services, pool size ≈ CPU cores; for I/O-bound, size can be higher.

### 6. Dependency Injection - Constructor Only

```kotlin
// ✅ CORRECT: Constructor injection (for infrastructure beans)
@Repository
class UserStoreR2DbcRepository(
    private val r2dbcRepository: UserR2DbcRepository,
    private val mapper: UserMapper,
) : UserRepository {
    // Implementation
}

// ✅ CORRECT: Application services via @Configuration (NO @Service on them)
@Configuration
class UserConfiguration {
    @Bean
    fun userCreator(repository: UserRepository) = UserCreator(repository)
}

// ❌ WRONG: Field injection
@Repository
class UserStoreR2DbcRepository {
    @Autowired
    private lateinit var r2dbcRepository: UserR2DbcRepository  // NEVER!
}
```

## HTTP Status Codes

| Code                        | When to Use                             |
| --------------------------- | --------------------------------------- |
| `200 OK`                    | Successful GET, PUT                     |
| `201 Created`               | Successful POST that creates resource   |
| `204 No Content`            | Successful DELETE                       |
| `400 Bad Request`           | Invalid input, malformed JSON           |
| `401 Unauthorized`          | Missing or invalid auth                 |
| `403 Forbidden`             | Valid auth but insufficient permissions |
| `404 Not Found`             | Resource doesn't exist                  |
| `409 Conflict`              | State conflict (duplicate email, etc.)  |
| `422 Unprocessable Entity`  | Valid syntax but semantic errors        |
| `500 Internal Server Error` | Unexpected server errors                |

## WebFlux - NEVER Block

```kotlin
// ❌ NEVER use block() in application code
val user = userRepository.findById(id).block()  // WRONG!

// ✅ Use coroutines with suspend
suspend fun getUser(id: UUID): User {
    return userRepository.findById(id).awaitSingleOrNull()
        ?: throw NotFoundException("User not found")
}

// ✅ Or use reactive operators
fun getUser(id: UUID): Mono<User> {
    return userRepository.findById(id)
        .switchIfEmpty(Mono.error(NotFoundException("User not found")))
}
```

### Mixing Reactive and Coroutine Paradigms

Spring 6.1+ seamlessly bridges `Mono`/`Flux` and Kotlin coroutines. Choose one model per API boundary:

**Guidelines:**

| Scenario                       | Recommendation                                   |
| ------------------------------ | ------------------------------------------------ |
| Service/handler APIs           | Prefer `suspend fun` for coroutine-first design  |
| Library code / reactive chains | Use `Mono<T>`/`Flux<T>` for composability        |
| WebClient calls                | Convert at boundary, don't suspend inside chains |

**Safe Conversions:**

```kotlin
// Consuming Mono/Flux in coroutines
suspend fun getUser(id: UUID): User? {
    return userRepository.findById(id).awaitSingleOrNull()  // Mono -> suspend
}

suspend fun getAllUsers(): List<User> {
    return userRepository.findAll().collectList().awaitSingle()  // Flux -> List
}

// Exposing suspend as Mono/Flux
fun getUserMono(id: UUID): Mono<User> = mono {
    getUserSuspend(id)  // suspend fun -> Mono
}

fun getUsersFlux(): Flux<User> = flux {
    getAllUsersSuspend().forEach { emit(it) }  // suspend fun -> Flux
}
```

**⚠️ Avoid suspending inside reactive chains:**

```kotlin
// ❌ WRONG: Don't mix suspend calls inside reactive operators
fun findUserWithOrg(userId: UUID): Mono<UserWithOrg> {
    return userRepository.findById(userId)
        .flatMap { user ->
            // Don't call suspend functions here!
            runBlocking { orgService.findById(user.orgId) }  // WRONG!
        }
}

// ✅ CORRECT: Keep reactive or convert at boundary
fun findUserWithOrg(userId: UUID): Mono<UserWithOrg> {
    return userRepository.findById(userId)
        .flatMap { user ->
            orgRepository.findById(user.orgId)  // Stay reactive
                .map { org -> UserWithOrg(user, org) }
        }
}

// ✅ CORRECT: Or use suspend throughout
suspend fun findUserWithOrg(userId: UUID): UserWithOrg? {
    val user = userRepository.findById(userId).awaitSingleOrNull() ?: return null
    val org = orgRepository.findById(user.orgId).awaitSingleOrNull()
    return UserWithOrg(user, org)
}
```

## Security Configuration

```kotlin
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/public/**").permitAll()
                    .pathMatchers("/api/admin/**").hasRole("ADMIN")
                    .pathMatchers("/api/**").authenticated()
                    .anyExchange().permitAll()
            }
            .oauth2ResourceServer { it.jwt {} }
            .build()
    }
}
```

## Application Profiles

| Profile   | Purpose                                               |
| --------- | ----------------------------------------------------- |
| `dev`     | Local development, verbose logging, H2/Testcontainers |
| `test`    | Automated tests, mocked external services             |
| `staging` | Production-like, real services                        |
| `prod`    | Production, optimized settings, real databases        |

```yaml
# application-dev.yml
logging:
  level:
    com.cvix: DEBUG
    org.springframework: INFO

# application-prod.yml
logging:
  level:
    com.cvix: INFO
    org.springframework: WARN
```

## Anti-Patterns

❌ **Spring annotations in Application/Domain layers** - Only Infrastructure has Spring
❌ **Business logic in controllers** - Controllers delegate to application handlers
❌ **Exposing entities in API** - Use DTOs (Request/Response)
❌ **`block()` in WebFlux** - Use coroutines or reactive operators
❌ **Generic exception catching** - Handle specific exceptions
❌ **Secrets in code** - Use environment variables
❌ **Domain repository port interfaces extending Spring interfaces** - Domain ports like `UserRepository` must remain pure Kotlin interfaces; only the infrastructure adapter (e.g., `UserR2DbcRepository`) may extend `ReactiveCrudRepository` or other Spring interfaces

## Commands

```bash
# Run application
./gradlew bootRun

# Run with profile
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# Run tests
./gradlew test

# Integration tests only
./gradlew test -PincludeTags=integration

# Build JAR
./gradlew bootJar
```

## Resources

- [hexagonal-architecture skill](.ruler/skills/hexagonal-architecture/SKILL.md) - Domain,
  Application layers & feature organization
- [kotlin skill](.ruler/skills/kotlin/SKILL.md) - Kotlin conventions for all layers
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [Kotlin Coroutines with Spring](https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html)
