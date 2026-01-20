package com.cvix.users.infrastructure.http

import com.cvix.users.application.response.UserResponse
import com.cvix.users.infrastructure.service.AccountResourceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import java.security.Principal
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private const val USER_ACCOUNT_INFO_EXAMPLE = """
    {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "authorities": ["ROLE_USER", "ROLE_ADMIN"]
    }
"""

/**
 * The [AccountResourceController] class is responsible for handling HTTP requests related to account information.
 * It is a Spring RestController with the base path*/
@Tag(
    name = "User Account",
    description = "User account management and profile endpoints",
)
@RestController
@RequestMapping("/api", produces = ["application/vnd.api.v1+json"])
class AccountResourceController(private val accountResourceService: AccountResourceService) {
    /**
     * Gets the account information for the authenticated user.
     *
     * @param principal the Principal object representing the authenticated user.
     * @return a Mono object encapsulating the UserResponse containing the account information.
     */
    @Operation(
        summary = "Get current user account information",
        description = "Retrieves the profile information and account details for the currently authenticated user. " +
            "Information is derived from the security principal established via JWT token.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Account information retrieved successfully",
                content = [
                    Content(
                        mediaType = "application/vnd.api.v1+json",
                        schema = Schema(implementation = UserResponse::class),
                        examples = [
                            ExampleObject(
                                name = "User account info",
                                value = USER_ACCOUNT_INFO_EXAMPLE,
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during account lookup",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping("/account")
    fun getAccount(principal: Principal): Mono<UserResponse> {
        log.debug("Getting user account information")

        return accountResourceService.getAccount(principal as AbstractAuthenticationToken)
    }
    companion object {
        private val log = LoggerFactory.getLogger(AccountResourceController::class.java)
    }
}
