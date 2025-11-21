package com.loomify.resume.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.engine.AppConstants
import com.loomify.resume.application.ResumeDocumentResponse
import com.loomify.resume.application.get.GetResumeQuery
import com.loomify.resume.domain.exception.ResumeNotFoundException
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Pattern
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 *
 * @created 20/11/25
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class GetResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "Get a resume by ID",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Resume retrieved successfully"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "404", description = "Resume not found"),
    )
    @GetMapping("/resume/{id}")
    suspend fun getResume(
        @PathVariable
        @Pattern(
            regexp = AppConstants.UUID_PATTERN,
            message = "Invalid UUID format",
        )
        id: String,
    ): ResponseEntity<ResumeDocumentResponse> {
        val userId = UUID.fromString(
            userId() ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Missing user ID in token",
            ),
        )

        val query = GetResumeQuery(id = UUID.fromString(id), userId = userId)

        return try {
            val document = ask(query)
            ResponseEntity.ok(document)
        } catch (e: ResumeNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        }
    }
}
