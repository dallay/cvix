package com.cvix.users.infrastructure.http

import com.cvix.AppConstants.Paths.API
import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.common.domain.error.BusinessRuleValidationException
import com.cvix.common.domain.vo.credential.CredentialException
import com.cvix.spring.boot.ApiController
import com.cvix.users.infrastructure.http.request.RegisterUserRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ConstraintViolationException
import java.net.URI
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.support.WebExchangeBindException

/**
 * Controller for user registration operations.
 *
 * This controller handles new user account creation with comprehensive validation:
 * - Email format and uniqueness validation
 * - Password strength requirements enforcement
 * - User data validation (firstname, lastname)
 * - Duplicate email detection
 *
 * Uses header-based API versioning (API-Version: v1).
 *
 * ## Validation Rules:
 * - Email: Valid format, max 255 chars, must be unique
 * - Password: Min 8 chars, must include uppercase, lowercase, digit, and special char
 * - Names: Min 1 char, max 100 chars each
 *
 * ## Security Features:
 * - Password complexity validation
 * - Email sanitization to prevent injection attacks
 * - Rate limiting to prevent abuse
 *
 * @property mediator The mediator for dispatching commands.
 * @created 2/7/23
 */
@Validated
@RestController
@RequestMapping(value = [API], produces = ["application/vnd.api.v1+json"])
@Tag(
    name = "User Registration",
    description = "User account creation and registration endpoints",
)
class UserRegisterController(
    mediator: Mediator,
) : ApiController(mediator) {

    /**
     * Registers a new user account.
     *
     * Creates a new user with the provided email, password, and personal information.
     * The email must be unique and not already registered in the system.
     * The password must meet security complexity requirements.
     *
     * On success, returns HTTP 201 Created with a Location header pointing to the new user resource.
     *
     * @param registerUserRequest The registration request containing user data.
     * @return ResponseEntity with HTTP 201 and Location header, or error response.
     */
    @Operation(
        summary = "Register new user account",
        description = "Creates a new user account with email, password, and personal information. " +
            "Validates email uniqueness, password complexity, and user data. " +
            "Returns HTTP 201 Created with Location header on success.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User account created successfully - Empty response body with Location header " +
                    "containing user resource URI",
                headers = [
                    Header(
                        name = "Location",
                        description = "URI of the created user resource (e.g., /users/{userId})",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data - Validation errors (invalid email format, weak password, " +
                    "missing required fields, or invalid field lengths)",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Email address already registered in the system",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too many registration attempts - Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during user registration",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PostMapping("/auth/register", consumes = ["application/json"])
    suspend fun registerUser(
        @Validated @RequestBody registerUserRequest: RegisterUserRequest,
    ): ResponseEntity<Void> {
        log.debug(
            "Registering new user with email: {}",
            StringEscapeUtils.escapeJava(registerUserRequest.email),
        )

        return try {
            val userId = dispatch(registerUserRequest.toRegisterUserCommand())

            log.info("User registered successfully with ID: {}", userId)

            ResponseEntity.created(
                URI.create("/users/$userId"),
            ).build()
        } catch (error: WebExchangeBindException) {
            log.debug("Validation error during user registration: {}", error.message)
            ResponseEntity.badRequest().build()
        } catch (error: ConstraintViolationException) {
            log.debug("Constraint violation during user registration: {}", error.message)
            ResponseEntity.badRequest().build()
        } catch (error: CredentialException) {
            log.debug("Password validation error during user registration: {}", error.message)
            ResponseEntity.badRequest().build()
        } catch (error: BusinessRuleValidationException) {
            log.debug("Business rule validation error during user registration: {}", error.message)
            ResponseEntity.badRequest().build()
        } catch (error: DataIntegrityViolationException) {
            log.debug("Data integrity violation during user registration (likely duplicate email): {}", error.message)
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (error: CommandHandlerExecutionError) {
            log.warn("Command execution error during user registration: {}", error.message)

            // Check if it's a domain validation error that should return 400
            when {
                isDomainValidationError(error) -> {
                    log.debug("Treating command execution error as validation error: {}", error.message)
                    ResponseEntity.badRequest().build()
                }

                error.cause is CredentialException -> {
                    log.debug("Password validation error in command execution: {}", error.cause?.message)
                    ResponseEntity.badRequest().build()
                }

                error.cause is BusinessRuleValidationException -> {
                    log.debug("Business rule validation error in command execution: {}", error.cause?.message)
                    ResponseEntity.badRequest().build()
                }

                else -> {
                    log.error("Unexpected command execution error during user registration", error)
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
        } catch (@Suppress("TooGenericExceptionCaught") error: Throwable) {
            log.error("Unexpected error during user registration", error)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Determines if a CommandHandlerExecutionError is a domain validation error.
     *
     * Checks error message for validation-related keywords to distinguish between
     * validation errors (400) and server errors (500).
     *
     * @param error The command handler execution error.
     * @return true if the error is a validation error, false otherwise.
     */
    private fun isDomainValidationError(error: CommandHandlerExecutionError): Boolean {
        val message = error.message?.lowercase() ?: ""
        val causeMessage = error.cause?.message?.lowercase() ?: ""

        val validationKeywords = listOf(
            "already exists",
            "invalid",
            "duplicate",
            "constraint",
            "conflict",
            "validation",
            "password",
            "email",
            "weak",
            "complexity",
        )

        return validationKeywords.any { keyword ->
            message.contains(keyword) || causeMessage.contains(keyword)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserRegisterController::class.java)
    }
}
