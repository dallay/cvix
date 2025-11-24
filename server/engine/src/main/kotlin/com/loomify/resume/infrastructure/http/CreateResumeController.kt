package com.loomify.resume.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.common.domain.bus.command.CommandHandlerExecutionError
import com.loomify.resume.application.create.CreateResumeCommand
import com.loomify.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.loomify.resume.infrastructure.http.request.CreateResumeRequest
import com.loomify.spring.boot.ApiController
import com.loomify.spring.boot.logging.LogMasker
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import java.net.URI
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for creating resumes.
 * @created 20/11/25
 */
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class CreateResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "Create a new resume",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Resume created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
    )
    @PutMapping("/resume/{id}")
    suspend fun createResume(
        @PathVariable
        id: UUID,
        @Valid @RequestBody request: CreateResumeRequest
    ): ResponseEntity<String> {
        val safeIdMasked = LogMasker.mask(id)
        log.debug("Creating new resume: {}", safeIdMasked)
        val userId = userIdFromToken()

        val workspaceId = request.workspaceId

        val command = CreateResumeCommand(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = request.title,
            content = ResumeRequestMapper.toDomain(request.content),
            createdBy = userId.toString(),
        )
        try {
            dispatch(command)
        } catch (e: CommandHandlerExecutionError) {
            log.error("Error creating resume/cv with ID: {}", safeIdMasked, e)
            throw e
        }
        return ResponseEntity.created(
            URI.create("/api/resume/${sanitizePathVariable(id.toString())}"),
        ).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateResumeController::class.java)
    }
}
