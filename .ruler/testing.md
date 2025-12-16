# Testing Guidelines

> Comprehensive testing strategy covering unit tests, integration tests, and E2E tests for both backend (Kotlin) and frontend (TypeScript).

## Testing Strategy

### The Testing Pyramid

We follow the principles of the testing pyramid to ensure a balanced and effective testing portfolio:

1. **Unit Tests (Base)**: The largest number of tests. Fast, isolated, verify small pieces of code.
2. **Integration Tests (Middle)**: Fewer than unit tests. Verify that different parts of the system work together correctly.
3. **End-to-End (E2E) Tests (Top)**: The smallest number of tests. Simulate full user journeys, slowest and most brittle.

### General Principles (All Tests)

- **CI/CD**: All tests (Unit and Integration) run automatically in GitHub Actions on every commit
- **Code Coverage**: Focus on testing behavior rather than just lines of code. Use Kover (backend) and Vitest coverage (frontend)
- **Test Naming**: Follow the `should do something when condition` pattern

---

## Backend Testing (Kotlin & Spring Boot)

### General Principles (Kotlin Testing)

- **Arrange-Act-Assert (AAA)**: Structure tests clearly:
  1. **Arrange**: Set up test objects, mocks, and data
  2. **Act**: Invoke the method or code under test
  3. **Assert**: Verify the outcome is as expected
- **Readability**: Tests serve as living documentation for your code's behavior
- **Isolation**: Unit tests must be completely isolated from external systems

### Naming and Structure

- **Location**: Test files in the same package as the class they test, under `src/test/kotlin`
- **Class Naming**: `<ClassName>Test` (e.g., `UserService` → `UserServiceTest`)
- **Method Naming**: `` `should do something when condition` `` in backticks

### Annotations and Base Classes

| Annotation/Class | Purpose |
|-----------------|---------|
| `@UnitTest` | Mark unit tests for clear scope indication |
| `@IntegrationTest` | Composite annotation for tests requiring full Spring Boot context |
| `ControllerIntegrationTest` | Abstract base class for controller tests with pre-configured `WebTestClient`, CSRF, and JWT helpers |

### Unit Testing

- **Scope**: Single class or unit of logic in isolation (domain models, services, utility functions)
- **Mocking**: Use **MockK** for all external dependencies
- **Frameworks**: **JUnit 5** as test runner, **Kotest** for assertions

```kotlin
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test

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

### Integration Testing

- **Scope**: Interaction between multiple components (service + database, controller + service)
- **Test Slices**:
  - `@DataR2dbcTest`: Persistence layer with Testcontainers
  - `@WebFluxTest`: Web layer with mocked services
- **Database**: Use **Testcontainers** with PostgreSQL for realistic environments

#### Controller Integration Test

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

#### Repository Integration Test

```kotlin
@DataR2dbcTest
@Import(TestcontainersConfiguration::class)
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

### Assertions with Kotest

```kotlin
result shouldBe expected
list shouldHaveSize 10
string shouldContain "substring"
exception shouldHaveMessage "Error message"
```

### Mocking with MockK

```kotlin
// Create mocks
val repository: UserRepository = mockk()

// Stub suspend functions
coEvery { repository.findById(any()) } returns Mono.just(user)

// Stub regular functions
every { service.process(any()) } returns result

// Verify interactions
coVerify { repository.save(any()) }
verify { service.process(any()) }
```

---

## Frontend Testing (TypeScript & Vue)

### Unit Testing (Vue)

- **Framework**: **Vitest** with `@testing-library/vue`
- **Scope**: Components, composables, and utility functions
- **Location**: Place test files alongside the component using `.spec.ts` or `.test.ts` suffixes

### Testing Approach

- Test all components, composables, and utility functions
- Use `@testing-library/vue` for component interaction
- Focus on behavior, not implementation details

---

## End-to-End Testing (Playwright)

### Guiding Principles

- **User-Facing Locators**: Prioritize `getByRole`, `getByLabel`, `getByText` for resilience and accessibility
- **Web-First Assertions**: Use Playwright's auto-retrying assertions (e.g., `await expect(locator).toBeVisible()`)
- **Avoid**: Manual waits, sleeps, brittle XPath or CSS class selectors

### Test Structure

```typescript
import { test, expect } from '@playwright/test';

test.describe('User Login Feature', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('should allow a user to log in with valid credentials', async ({ page }) => {
    await test.step('Fill and submit login form', async () => {
      await page.getByLabel('Email').fill('test@example.com');
      await page.getByLabel('Password').fill('password123');
      await page.getByRole('button', { name: 'Log In' }).click();
    });

    await test.step('Verify successful navigation', async () => {
      await expect(page).toHaveURL('/dashboard');
      await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
    });
  });
});
```

### File Organization

- **Location**: `client/e2e/` directory
- **Naming**: `<feature>.spec.ts` (e.g., `login.spec.ts`)

### Assertion Best Practices

| Assertion | Purpose |
|-----------|---------|
| `toBeVisible()` | Element is in DOM and visible |
| `toHaveText()` | Exact text match |
| `toContainText()` | Partial text match |
| `toHaveURL()` | Verify page URL after action |
| `toHaveCount()` | Assert number of elements |

### Test Execution

```bash
# Run headlessly in CI
pnpm test:e2e

# Debug in headed mode
pnpm test:e2e --headed
```

---

## Coverage Requirements

- **Backend**: Use Kover to track Kotlin code coverage
- **Frontend**: Use Vitest coverage
- **Goal**: High coverage, but prioritize meaningful tests over arbitrary percentages
- All tests must be idempotent and run independently of each other
