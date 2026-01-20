package com.cvix.authentication.infrastructure.http

import com.cvix.AppConstants.Paths.API
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Tag(
    name = "Security",
    description = "Security and CSRF protection endpoints",
)
@RestController
@RequestMapping(value = [API], produces = ["application/vnd.api.v1+json"])
class CsrfController {
    @Operation(
        summary = "Retrieve CSRF protection token",
        description = "Provides a CSRF (Cross-Site Request Forgery) token to be used in state-changing requests. " +
            "This token must be included in subsequent POST, PUT, DELETE, or PATCH requests as a header " +
            "or parameter as configured in the security policy. Helps prevent CSRF attacks in browser-based sessions.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "CSRF token retrieved successfully",
                content = [
                    Content(
                        mediaType = "application/vnd.api.v1+json",
                        schema = Schema(type = "object"),
                        examples = [
                            ExampleObject(
                                name = "CSRF token response",
                                value = """{"csrf": "ok"}""",
                            ),
                        ],
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during CSRF token generation",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping("/auth/csrf")
    fun getCsrfToken(): Mono<ResponseEntity<Map<String, String>>> =
        Mono.just(ResponseEntity.ok(mapOf("csrf" to "ok")))
}
