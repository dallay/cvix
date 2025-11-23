package com.loomify.resume.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.resume.application.delete.DeleteResumeCommand
import com.loomify.resume.domain.exception.ResumeAccessDeniedException
import com.loomify.resume.domain.exception.ResumeNotFoundException
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * REST controller for deleting resumes.
 * @created 20/11/25
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class DeleteResumeController(
    mediator: Mediator,
) : ApiController(mediator) {

    @Operation(
        summary = "Delete a resume by ID",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Resume deleted successfully"),
        ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid identifier or token subject"
        ),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "403", description = "Forbidden - Access denied to this resume"),
        ApiResponse(responseCode = "404", description = "Resume not found"),
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
