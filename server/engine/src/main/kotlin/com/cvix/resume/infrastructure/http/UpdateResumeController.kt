package com.cvix.resume.infrastructure.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.common.domain.bus.command.CommandHandlerExecutionError
import com.cvix.resume.application.ResumeDocumentResponse
import com.cvix.resume.application.update.UpdateResumeCommand
import com.cvix.resume.domain.exception.ResumeNotFoundException
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.UpdateResumeRequest
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class UpdateResumeController(
    mediator: Mediator,
) : ApiController(mediator) {
    @Operation(
        summary = "Update an existing resume/CV document",
        description = "Modifies the content and metadata of an existing resume within a workspace. " +
            "Supports optimistic locking with expectedUpdatedAt parameter to prevent concurrent modifications. " +
            "Requires authentication and workspace context. Returns updated resume document on success.",
        security = [SecurityRequirement(name = "bearerAuth")],
        parameters = [
            Parameter(
                name = "X-Workspace-Id",
                description = "The ID of the workspace context",
                required = true,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", format = "uuid"),
                example = "123e4567-e89b-12d3-a456-426614174000",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Resume updated successfully - Returns updated resume document with Location header",
                headers = [
                    Header(
                        name = "Location",
                        description = "URI of the updated resume resource",
                        schema = Schema(type = "string")
                    )
                ],
                content = [
                    Content(
                        mediaType = "application/vnd.api.v1+json",
                        schema = Schema(implementation = ResumeDocumentResponse::class),
                        examples = [
                            ExampleObject(
                                name = "Updated resume",
                                value = """
                                {
                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                    "title": "Senior Software Engineer Resume",
                                    "content": {
                                        "sections": [
                                            {
                                                "type": "summary",
                                                "content": "Experienced software engineer with 5+ years..."
                                            }
                                        ]
                                    },
                                    "createdAt": "2024-01-15T10:30:00Z",
                                    "updatedAt": "2024-01-20T14:25:00Z",
                                    "createdBy": "john.doe@example.com",
                                    "updatedBy": "john.doe@example.com"
                                }
                                """
                            )
                        ]
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid resume data structure or validation errors",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Invalid content structure",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/400",
                                    "title": "Bad Request",
                                    "status": 400,
                                    "detail": "Invalid resume content structure: missing required sections",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Missing or invalid authentication token",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Missing authentication",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/401",
                                    "title": "Unauthorized",
                                    "status": 401,
                                    "detail": "Authentication required to update resume",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - User does not have permission to modify this resume",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Access denied",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/403",
                                    "title": "Forbidden",
                                    "status": 403,
                                    "detail": "You do not have permission to modify this resume",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Resume with specified ID does not exist",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Resume not found",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/404",
                                    "title": "Not Found",
                                    "status": 404,
                                    "detail": "Resume with ID 550e8400-e29b-41d4-a716-446655440000 not found",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Resume was modified by another user (optimistic lock failure)",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Concurrent modification",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/409",
                                    "title": "Conflict",
                                    "status": 409,
                                    "detail": "Resume was modified by another user. Please refresh and try again.",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Unprocessable entity - Validation failed for update data",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Validation error",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/422",
                                    "title": "Unprocessable Entity",
                                    "status": 422,
                                    "detail": "Title must be between 1 and 200 characters",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error - Failed to update resume due to system error",
                content = [
                    Content(
                        schema = Schema(implementation = org.springframework.http.ProblemDetail::class),
                        examples = [
                            ExampleObject(
                                name = "Update error",
                                value = """
                                {
                                    "type": "https://httpstatuses.com/500",
                                    "title": "Internal Server Error",
                                    "status": 500,
                                    "detail": "Failed to update resume due to internal error",
                                    "instance": "/api/resume/550e8400-e29b-41d4-a716-446655440000/update"
                                }
                                """
                            )
                        ]
                    )
                ],
            ),
        ]
    )
    @PutMapping("/resume/{id}/update")
    suspend fun updateResume(
        @Parameter(
            name = "id",
            description = "Unique identifier of the resume to update. Must be a valid UUID.",
            required = true,
            `in` = ParameterIn.PATH,
            schema = Schema(type = "string", format = "uuid"),
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @PathVariable
        id: UUID,
        @RequestBody(
            description = "Resume update data containing new title, content, and optional expected timestamp for optimistic locking",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UpdateResumeRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Standard update",
                            description = "Update resume with new title and content",
                            value = """
                            {
                                "title": "Senior Software Engineer Resume - Updated",
                                "content": {
                                    "sections": [
                                        {
                                            "type": "summary",
                                            "content": "Experienced software engineer with 6+ years in full-stack development..."
                                        },
                                        {
                                            "type": "experience",
                                            "content": {
                                                "company": "Tech Corp",
                                                "position": "Senior Developer",
                                                "duration": "2020-Present"
                                            }
                                        }
                                    ]
                                }
                            }
                            """
                        ),
                        ExampleObject(
                            name = "Update with optimistic locking",
                            description = "Update with expected timestamp to prevent concurrent modifications",
                            value = """
                            {
                                "title": "Updated Resume Title",
                                "content": {
                                    "sections": [
                                        {
                                            "type": "contact",
                                            "content": {
                                                "email": "john.doe@example.com",
                                                "phone": "+1-555-0123"
                                            }
                                        }
                                    ]
                                },
                                "expectedUpdatedAt": "2024-01-20T14:25:00Z"
                            }
                            """
                        )
                    ]
                )
            ]
        )
        @Valid @Validated 
        request: UpdateResumeRequest
    ): ResponseEntity<ResumeDocumentResponse> {
        val sanitizedId = sanitizePathVariable(id.toString())
        log.debug("Update resume {}", sanitizedId)
        val userId = userIdFromToken()
        val workspaceId = workspaceIdFromContext()

        val command = UpdateResumeCommand(
            id = id,
            userId = userId,
            workspaceId = workspaceId,
            title = request.title,
            content = ResumeRequestMapper.toDomain(request.content),
            updatedBy = userEmail() ?: userId.toString(),
            expectedUpdatedAt = request.expectedUpdatedAt?.let { java.time.Instant.parse(it) },
        )
        val response = try {
            dispatch(command)
        } catch (e: ResumeNotFoundException) {
            log.warn("Resume not found for update: {}", sanitizedId, e)
            return ResponseEntity.notFound().build()
        } catch (e: CommandHandlerExecutionError) {
            log.error("Error updating resume with ID: {}", sanitizedId, e)
            throw e
        }
        return ResponseEntity
            .ok()
            .location(URI.create("/api/resume/$sanitizedId"))
            .body(response)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateResumeController::class.java)
    }
}
