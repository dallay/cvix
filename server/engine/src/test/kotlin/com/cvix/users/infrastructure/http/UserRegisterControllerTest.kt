package com.cvix.users.infrastructure.http

import com.cvix.UnitTest
import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.common.domain.error.BusinessRuleValidationException
import com.cvix.common.domain.vo.credential.CredentialException
import com.cvix.users.application.register.RegisterUserCommand
import com.cvix.users.infrastructure.http.request.RegisterUserRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import java.util.stream.Stream
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

private const val ENDPOINT = "/api/auth/register"

@UnitTest
@DisplayName("UserRegisterController Unit Tests")
class UserRegisterControllerTest {
    private val faker = Faker()
    private val email = faker.internet().emailAddress()
    private val password = faker.credentials().password(8, 80, true, true, true)
    private val firstname = faker.name().firstName()
    private val lastname = faker.name().lastName()

    private val mediator: Mediator = mockk()
    private val userRegisterController = UserRegisterController(mediator)
    private val webTestClient = WebTestClient.bindToController(userRegisterController).build()

    companion object {
        @JvmStatic
        fun validationErrorScenarios(): Stream<Arguments> = Stream.of(
            Arguments.of(
                RegisterUserRequest("invalid-email", "ValidPassword1", "John", "Doe"),
                "Invalid email format",
                0, // Mediator not called
            ),
            Arguments.of(
                RegisterUserRequest("valid@email.com", "short", "John", "Doe"),
                "Password too short",
                0, // Mediator not called
            ),
            Arguments.of(
                RegisterUserRequest("valid@email.com", "ValidPassword1", "", "Doe"),
                "Blank firstname",
                0, // Mediator not called
            ),
            Arguments.of(
                RegisterUserRequest("valid@email.com", "ValidPassword1", "John", ""),
                "Blank lastname",
                0, // Mediator not called
            ),
        )

        @JvmStatic
        fun exceptionValidationScenarios(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CredentialException("The password must have at least one number"),
                "Password complexity validation error",
            ),
            Arguments.of(
                object : BusinessRuleValidationException("Invalid business rule") {},
                "Business rule validation error",
            ),
        )

        @JvmStatic
        fun conflictScenarios(): Stream<Arguments> = Stream.of(
            Arguments.of(
                DataIntegrityViolationException("Duplicate key"),
                "Data integrity violation",
            ),
        )

        @JvmStatic
        fun badRequestErrorMessages(): Stream<Arguments> = Stream.of(
            Arguments.of("User already exists", "Domain validation error"),
            Arguments.of("Email is invalid or duplicate", "Email validation error"),
            Arguments.of("Constraint validation failed", "Constraint violation error"),
        )

        @JvmStatic
        fun serverErrorScenarios(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CommandHandlerExecutionError("Database connection failed"),
                "Server error from command",
            ),
            Arguments.of(RuntimeException("Unexpected error"), "Unexpected runtime error"),
        )

        @JvmStatic
        fun wrappedExceptions(): Stream<Arguments> = Stream.of(
            Arguments.of(
                CredentialException("The password must have at least one number"),
                "Password validation error",
            ),
            Arguments.of(
                object : BusinessRuleValidationException("Business rule violated") {},
                "Business rule validation error",
            ),
        )
    }

    @Nested
    @DisplayName("Successful Registration Tests")
    inner class SuccessfulRegistrationTests {
        @Test
        @DisplayName("Should register user successfully and return 201 with location header")
        fun `should register user successfully and return 201 with location header`(): Unit =
            runTest {
                // Given
                val expectedUserId = UUID.randomUUID()
                val request = RegisterUserRequest(email, password, firstname, lastname)
                coEvery { mediator.send(any<RegisterUserCommand>()) } returns expectedUserId

                // When & Then
                webTestClient.post().uri(ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated
                    .expectHeader().location("/users/$expectedUserId")
                    .expectBody().isEmpty

                // Verify the command was sent with correct parameters
                coVerify(exactly = 1) {
                    mediator.send(
                        match<RegisterUserCommand> { command ->
                            command.email == email &&
                                command.password == password &&
                                command.firstname == firstname &&
                                command.lastname == lastname
                        },
                    )
                }
            }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    inner class ValidationErrorTests {

        @ParameterizedTest(name = "Should return 400 for {1}")
        @MethodSource(
            "com.cvix.users.infrastructure.http.UserRegisterControllerTest#validationErrorScenarios",
        )
        fun `should return 400 for input validation errors`(
            request: RegisterUserRequest,
            @Suppress("UNUSED_PARAMETER") testCase: String,
            mediatorCallCount: Int,
        ): Unit = runTest {
            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest

            coVerify(exactly = mediatorCallCount) { mediator.send(any<RegisterUserCommand>()) }
        }

        @ParameterizedTest(name = "Should handle {1} and return 400")
        @MethodSource(
            "com.cvix.users.infrastructure.http.UserRegisterControllerTest#exceptionValidationScenarios",
        )
        fun `should handle exceptions and return 400`(
            exception: Exception,
            @Suppress("UNUSED_PARAMETER") testCase: String,
        ): Unit = runTest {
            // Given
            val request = RegisterUserRequest(email, password, firstname, lastname)
            coEvery { mediator.send(any<RegisterUserCommand>()) } throws exception

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().isEmpty

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }

        @ParameterizedTest(name = "Should handle {1} and return 409")
        @MethodSource("com.cvix.users.infrastructure.http.UserRegisterControllerTest#conflictScenarios")
        fun `should handle conflict exceptions and return 409`(
            exception: Exception,
            @Suppress("UNUSED_PARAMETER") testCase: String,
        ): Unit = runTest {
            // Given
            val request = RegisterUserRequest(email, password, firstname, lastname)
            coEvery { mediator.send(any<RegisterUserCommand>()) } throws exception

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().isEmpty

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }
    }

    @Nested
    @DisplayName("Command Handler Error Tests")
    inner class CommandHandlerErrorTests {

        @ParameterizedTest(name = "Should handle {1} and return 400")
        @MethodSource(
            "com.cvix.users.infrastructure.http.UserRegisterControllerTest#badRequestErrorMessages",
        )
        fun `should handle command execution errors and return 400`(
            errorMessage: String,
            @Suppress("UNUSED_PARAMETER") testCase: String,
        ): Unit = runTest {
            // Given
            val request = RegisterUserRequest(email, password, firstname, lastname)
            coEvery { mediator.send(any<RegisterUserCommand>()) } throws CommandHandlerExecutionError(
                errorMessage,
            )

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().isEmpty

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }

        @ParameterizedTest(name = "Should handle {1} wrapped in CommandHandlerExecutionError")
        @MethodSource("com.cvix.users.infrastructure.http.UserRegisterControllerTest#wrappedExceptions")
        fun `should handle exceptions wrapped in CommandHandlerExecutionError`(
            exception: Exception,
            @Suppress("UNUSED_PARAMETER") testCase: String,
        ): Unit = runTest {
            // Given
            val request = RegisterUserRequest(email, password, firstname, lastname)
            coEvery {
                mediator.send(any<RegisterUserCommand>())
            } throws CommandHandlerExecutionError(
                "Command execution failed",
                exception,
            )

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody().isEmpty

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }

        @ParameterizedTest(name = "Should handle {1} and return 500")
        @MethodSource("com.cvix.users.infrastructure.http.UserRegisterControllerTest#serverErrorScenarios")
        fun `should handle server errors and return 500`(
            exception: Exception,
            @Suppress("UNUSED_PARAMETER") testCase: String,
        ): Unit = runTest {
            // Given
            val request = RegisterUserRequest(email, password, firstname, lastname)
            coEvery { mediator.send(any<RegisterUserCommand>()) } throws exception

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError
                .expectBody().isEmpty

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    inner class EdgeCaseTests {
        @Test
        @DisplayName("Should handle maximum length valid inputs")
        fun `should handle maximum length valid inputs`(): Unit = runTest {
            // Given
            val longEmail = "very.long.email.address@very-long-domain-name.com"
            val longPassword = "ComplexPassword123!" + "x".repeat(50) // Long but valid password
            val longFirstname = "A".repeat(50)
            val longLastname = "B".repeat(50)
            val expectedUserId = UUID.randomUUID()
            val request = RegisterUserRequest(longEmail, longPassword, longFirstname, longLastname)

            coEvery { mediator.send(any<RegisterUserCommand>()) } returns expectedUserId

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated
                .expectHeader().location("/users/$expectedUserId")

            coVerify(exactly = 1) { mediator.send(any<RegisterUserCommand>()) }
        }

        @Test
        @DisplayName("Should handle special characters in names")
        fun `should handle special characters in names`(): Unit = runTest {
            // Given
            val specialFirstname = "José-María"
            val specialLastname = "O'Connor-Smith"
            val expectedUserId = UUID.randomUUID()
            val request = RegisterUserRequest(email, password, specialFirstname, specialLastname)

            coEvery { mediator.send(any<RegisterUserCommand>()) } returns expectedUserId

            // When & Then
            webTestClient.post().uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated

            coVerify(exactly = 1) {
                mediator.send(
                    match<RegisterUserCommand> { command ->
                        command.firstname == specialFirstname && command.lastname == specialLastname
                    },
                )
            }
        }
    }
}
