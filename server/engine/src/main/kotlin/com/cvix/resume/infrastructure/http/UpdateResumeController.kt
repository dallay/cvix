package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.common.domain.presentation.SimpleMessageResponse
import com.cvix.resume.application.update.UpdateResumeCommand
import com.cvix.resume.domain.exception.ResumeNotFoundException
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.UpdateResumeRequest
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import java.net.URI
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for updating resumes.
 * @created 20/11/25
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class UpdateResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "Update a resume by ID",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Resume updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "403", description = "Forbidden - Access denied to this resume"),
        ApiResponse(responseCode = "404", description = "Resume not found"),
        ApiResponse(
            responseCode = "409",
            description = "Conflict - Resume was modified by another user",
        ),
    )
    @PutMapping("/resume/{id}/update")
    suspend fun updateResume(
        @PathVariable
        id: UUID,
        @Valid @Validated @RequestBody request: UpdateResumeRequest
    ): ResponseEntity<SimpleMessageResponse> {
        val sanitizedId = sanitizePathVariable(id.toString())
        log.debug("Update resume {}", sanitizedId)
        val userId = userIdFromToken()

        val workspaceId = request.workspaceId

        val command = UpdateResumeCommand(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = request.title,
            content = ResumeRequestMapper.toDomain(request.content),
            updatedBy = userEmail() ?: userId.toString(),
            expectedUpdatedAt = request.expectedUpdatedAt?.let { java.time.Instant.parse(it) },
        )
        try {
            dispatch(command)
        } catch (e: ResumeNotFoundException) {
            log.warn("Resume not found for update: {}", sanitizedId, e)
            return ResponseEntity.notFound().build()
        } catch (e: CommandHandlerExecutionError) {
            log.error("Error creating resume with ID: {}", sanitizedId, e)
            throw e
        }
        return ResponseEntity
            .ok()
            .location(URI.create("/api/resume/$sanitizedId"))
            .body(SimpleMessageResponse("Resume with ID $sanitizedId updated successfully."))
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateResumeController::class.java)
    }
}
