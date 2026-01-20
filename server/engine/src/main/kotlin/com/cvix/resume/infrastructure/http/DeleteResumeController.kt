package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.delete.DeleteResumeCommand
import com.cvix.resume.domain.exception.ResumeAccessDeniedException
import com.cvix.resume.domain.exception.ResumeNotFoundException
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * REST controller for deleting resumes.
 * @created 20/11/25
 */
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class DeleteResumeController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(
        summary = "Delete a resume by ID",
        description = "Permanently deletes a resume document. Only the owner of the resume can perform this operation.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "id",
                description = "The unique UUID of the resume to delete",
                required = true,
                `in` = ParameterIn.PATH,
                example = "550e8400-e29b-41d4-a716-446655440000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Resume deleted successfully",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid identifier or token subject",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Access denied to this resume",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Resume not found",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @DeleteMapping("/resume/{id}")
    suspend fun deleteResume(
        @PathVariable
        id: UUID,
    ): ResponseEntity<Void> {
        val userId = userIdFromToken()

        val command = DeleteResumeCommand(id = id, userId = userId)

        return try {
            dispatch(command)
            log.debug("Successfully deleted resume by {}", id)
            ResponseEntity.noContent().build()
        } catch (e: ResumeNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
        } catch (e: ResumeAccessDeniedException) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message, e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DeleteResumeController::class.java)
    }
}
