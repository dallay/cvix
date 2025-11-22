package com.loomify.resume.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.command.CommandHandlerExecutionError
import com.loomify.engine.AppConstants
import com.loomify.resume.application.update.UpdateResumeCommand
import com.loomify.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.loomify.resume.infrastructure.http.request.UpdateResumeRequest
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import java.net.URI
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.HtmlUtils

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
        ApiResponse(responseCode = "404", description = "Resume not found"),
        ApiResponse(
            responseCode = "409",
            description = "Conflict - Resume was modified by another user",
        ),
    )
    @PutMapping("/resume/{id}/update")
    suspend fun updateResume(
        @PathVariable
        @Pattern(
            regexp = AppConstants.UUID_PATTERN,
            message = "Invalid UUID format",
        )
        id: String,
        @Valid @Validated @RequestBody request: UpdateResumeRequest
    ): ResponseEntity<String> {
        val userId = UUID.fromString(
            userId() ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Missing user ID in token",
            ),
        )

        val workspaceId = request.workspaceId

        val resumeId = UUID.fromString(id)
        val command = UpdateResumeCommand(
            id = resumeId,
            userId = userId,
            workspaceId = workspaceId,
            title = request.title,
            content = ResumeRequestMapper.toDomain(request.content),
            updatedBy = userEmail() ?: userId.toString(),
            expectedUpdatedAt = request.expectedUpdatedAt?.let { java.time.Instant.parse(it) },
        )
        try {
            dispatch(command)
        } catch (e: CommandHandlerExecutionError) {
            log.error(
                "Error creating workspace with ID: {}",
                sanitizePathVariable(resumeId.toString()),
                e,
            )
            throw e
        }
        val sanitizedId = HtmlUtils.htmlEscape(id)
        return ResponseEntity
            .ok()
            .location(URI.create("/api/resume/$sanitizedId"))
            .body(
                """
                    {
                        "message": "Resume with ID $sanitizedId updated successfully."
                    }
                """.trimIndent(),
            )
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateResumeController::class.java)
    }
}
