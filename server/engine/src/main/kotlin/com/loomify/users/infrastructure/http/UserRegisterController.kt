package com.loomify.users.infrastructure.http

import com.loomify.AppConstants.Paths.API
import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.command.CommandHandlerExecutionError
import com.loomify.common.domain.error.BusinessRuleValidationException
import com.loomify.common.domain.vo.credential.CredentialException
import com.loomify.spring.boot.ApiController
import com.loomify.users.application.response.UserResponse
import com.loomify.users.domain.ApiDataResponse
import com.loomify.users.infrastructure.http.request.RegisterUserRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.ConstraintViolationException
import java.net.URI
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.support.WebExchangeBindException

@RestController
@RequestMapping(value = [API], produces = ["application/vnd.api.v1+json"])
class UserRegisterController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(summary = "Register endpoint")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Created"),
        ApiResponse(responseCode = "400", description = "Bad request - Validation error or user already exists"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @PostMapping("/auth/register")
    suspend fun registerUser(@Validated @RequestBody registerUserRequest: RegisterUserRequest):
        ResponseEntity<ApiDataResponse<UserResponse>> {
        log.debug(
            "Registering new user with email: {}",
            StringEscapeUtils.escapeJava(registerUserRequest.email),
        )
        return try {
            val userId = dispatch(registerUserRequest.toRegisterUserCommand())
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
            ResponseEntity.badRequest().build()
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

    private fun isDomainValidationError(error: CommandHandlerExecutionError): Boolean {
        val message = error.message?.lowercase() ?: ""
        val causeMessage = error.cause?.message?.lowercase() ?: ""

        val validationKeywords = listOf(
            "already exists", "invalid", "duplicate", "constraint", "conflict",
            "validation", "password", "email", "weak", "complexity",
        )

        return validationKeywords.any { keyword ->
            message.contains(keyword) || causeMessage.contains(keyword)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserRegisterController::class.java)
    }
}
