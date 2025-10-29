# Backend Testing Conventions (Kotlin & Spring Boot)

> This document outlines the conventions and best practices for writing unit and integration tests for the Kotlin backend.

## General Principles

- **Arrange-Act-Assert (AAA)**: Structure your tests clearly following the AAA pattern.
  1.  **Arrange**: Set up the test objects, mocks, and data.
  2.  **Act**: Invoke the method or code under test.
  3.  **Assert**: Verify the outcome is as expected.
- **Readability**: Tests should be clear, concise, and easy to understand. They serve as living documentation for your code's behavior.
- **Isolation**: Unit tests must be completely isolated from external systems like databases or message queues. Integration tests should be isolated from other tests.

## Naming and Structure

- **Location**: Test files must be located in the same package as the class they are testing, under the `src/test/kotlin` directory.
- **Class Naming**: Test classes should be named after the class they test, with a `Test` suffix (e.g., `UserService` -> `UserServiceTest`).
- **Method Naming**: Test methods must describe the behavior they are testing, following the pattern: `` `should do something when condition` ``. Use backticks for readability.
  - Example: `` `should return user when user exists` ``

## Annotations and Base Classes

To ensure consistency, tests should use the following custom annotations and base classes:

- **Unit Tests**: Unit tests do not have a specific project annotation beyond the standard JUnit 5 `@Test`. Their defining characteristic is **isolation**, achieved by mocking all external dependencies. The class `RateLimitingFilterUnitTest` is a good example of this approach.
  - Unit tests must have the `@UnitTest` annotation to clearly indicate their scope.

- **`@IntegrationTest`**: A composite annotation for general integration tests that require a full Spring Boot application context. It applies a standard configuration and tags the test as `integration` for filtering purposes.
  - Integration tests that do not inherit from helper classes like `InfrastructureTestContainers` must include the `@IntegrationTest` annotation to explicitly mark them as integration tests.

- **`ControllerIntegrationTest`**: An abstract base class for controller-level integration tests. Tests for controllers should **extend this class**. It provides pre-configured `WebTestClient`, CSRF protection, and JWT authentication helpers, avoiding duplication.
  - Integration tests for controllers must inherit from the `ControllerIntegrationTest` helper class.

## Unit Testing

- **Scope**: Focus on testing a single class or unit of logic in isolation. This typically applies to domain models, services, and utility functions.
- **Mocking**: Use **MockK** to mock all external dependencies (e.g., repositories, other services).
- **Frameworks**: Use **JUnit 5** as the test runner and **Kotest** for assertions.

### Example Unit Test Structure

```kotlin
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import com.loomify.UnitTest

@UnitTest
class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val userService = UserService(userRepository)

    @Test
    fun `should return user when user exists`() {
        // Arrange
        val userId = UUID.randomUUID()
        val expectedUser = User(id = userId, name = "Test User")
        coEvery { userRepository.findById(userId) } returns Mono.just(expectedUser)

        // Act
        val result = userService.findById(userId).block()

        // Assert
        result shouldBe expectedUser
    }
}
```

## Integration Testing

- **Scope**: Test the interaction between multiple components, such as a service and the database, or a controller and the service layer.
- **Slices**: Use Spring Boot's test slices to load only the necessary parts of the application context.
  - **`@DataR2dbcTest`**: For testing the persistence layer (repositories). These tests should connect to a real database managed by **Testcontainers**.
  - **`@WebFluxTest`**: For testing the web layer (controllers). Mock the service layer to isolate the controller's logic.
- **Database**: Use **Testcontainers** with PostgreSQL for all tests that require a database. This ensures tests run against a realistic environment.

### Controller Integration Test (`@WebFluxTest`)

- Use **`WebTestClient`** to make requests to the endpoints.
- Use `@MockkBean` to inject mocks for service-layer dependencies.

```kotlin
@WebFluxTest(UserController::class)
class UserControllerTest : ControllerIntegrationTest() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var userService: UserService

    @Test
    fun `should return user details on GET`() {
        // Arrange
        val userId = UUID.randomUUID()
        val expectedUser = User(id = userId, name = "Test User")
        coEvery { userService.findById(userId) } returns Mono.just(expectedUser)

        // Act & Assert
        webTestClient.get().uri("/api/users/{id}", userId)
            .exchange()
            .expectStatus().isOk
            .expectBody(User::class.java)
    }
}
```

### Repository Integration Test (`@DataR2dbcTest`)

- These tests will automatically use the Testcontainers-provided database.
- Focus on verifying custom queries and repository behavior.

```kotlin
@DataR2dbcTest
@Import(TestcontainersConfiguration::class) // Assuming a config class for Testcontainers
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should find user by email`() {
        // Arrange
        val user = User(email = "test@example.com", name = "Test")
        userRepository.save(user).block()

        // Act
        val foundUser = userRepository.findByEmail("test@example.com").block()

        // Assert
        foundUser shouldNotBe null
        foundUser?.email shouldBe "test@example.com"
    }
}
```

## Assertions with Kotest

- Prefer Kotest's rich set of matchers for expressive and readable assertions.
- Examples:
  - `result shouldBe expected`
  - `list shouldHaveSize 10`
  - `string shouldContain "substring"`
  - `exception shouldHaveMessage "Error message"`

## Mocking with MockK

- Use `mockk()` to create mocks.
- Use `coEvery { ... } returns ...` for `suspend` functions.
- Use `every { ... } returns ...` for regular functions.
- Verify interactions with `coVerify` and `verify`.

## General Guidelines

- All tests must follow the principles of the testing pyramid (unit, integration, E2E).
- Use descriptive test names following the `should do something when condition` pattern.
- Ensure all tests are idempotent and can run independently of each other.

## Test Structure

- Organize tests by feature or module.
- All tests, including unit and integration tests, are located under the `src/test` directory.
- The differentiation between unit and integration tests is made using annotations:
  - Use `@UnitTest` for unit tests.
  - Use `@IntegrationTest` for integration tests.

## Coverage

- Aim for high code coverage, but prioritize meaningful tests over achieving arbitrary coverage percentages.
- Use Kover to track code coverage for Kotlin.
