package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.ResumeDocumentResponse
import com.cvix.resume.application.get.GetResumeQuery
import com.cvix.resume.domain.exception.ResumeAccessDeniedException
import com.cvix.resume.domain.exception.ResumeNotFoundException
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.ProblemDetail
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class GetResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "Get a resume by ID",
        description = "Retrieves a specific resume document by its unique UUID. " +
            "The user must be the owner of the resume to access it.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "id",
                description = "The unique UUID of the resume to retrieve",
                required = true,
                `in` = ParameterIn.PATH,
                example = "550e8400-e29b-41d4-a716-446655440000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Resume retrieved successfully",
                content = [Content(schema = Schema(implementation = ResumeDocumentResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have access to this resume",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Resume not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @GetMapping("/resume/{id}")
    suspend fun getResume(
        @PathVariable
        id: UUID,
    ): ResponseEntity<ResumeDocumentResponse> {
        val userId = userIdFromToken()

        val query = GetResumeQuery(id = id, userId = userId)

        return try {
            val document = ask(query)
            ResponseEntity.ok(document)
        } catch (e: ResumeNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        } catch (e: ResumeAccessDeniedException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
        }
    }
}
