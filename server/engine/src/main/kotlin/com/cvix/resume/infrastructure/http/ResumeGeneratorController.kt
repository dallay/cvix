package com.cvix.resume.infrastructure.http

import com.cvix.authentication.infrastructure.ApplicationSecurityProperties
import com.cvix.common.domain.bus.Mediator
import com.cvix.resume.application.generate.GenerateResumeCommand
import com.cvix.resume.domain.Locale
import com.cvix.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.cvix.resume.infrastructure.http.request.GenerateResumeRequest
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

private const val STANDARD_GENERATION_EXAMPLE = """
    {
        "templateId": "modern",
        "basics": {
            "name": "John Doe",
            "email": "john.doe@example.com",
            "label": "Software Engineer",
            "phone": "+1-555-0123"
        },
        "work": [
            {
                "name": "Tech Corp",
                "position": "Developer",
                "startDate": "2020-01-01"
            }
        ]
    }
"""

/**
 * REST controller for resume generation endpoints.
 */
@Tag(
    name = "Resume",
    description = "Resume/CV document management endpoints",
)
@RestController
@RequestMapping(value = ["/api/resume"], produces = ["application/vnd.api.v1+json"])
@Validated
class ResumeGeneratorController(
    mediator: Mediator,
    private val applicationSecurityProperties: ApplicationSecurityProperties,
) : ApiController(mediator) {

    @Operation(
        summary = "Generate a PDF resume from JSON Resume schema data",
        description = "Generates a professional PDF resume using the specified template and JSON Resume data. " +
            "Supports localization via the Accept-Language header. Maximum payload size is 100KB.",
        parameters = [
            Parameter(
                name = "Accept-Language",
                description = "Preferred language for the generated resume (e.g., 'en', 'es')",
                required = false,
                `in` = ParameterIn.HEADER,
                schema = Schema(type = "string", defaultValue = "en"),
                example = "en-US",
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Resume PDF generated successfully",
                content = [Content(mediaType = "application/pdf")],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data or unsupported locale",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Business rule validation failed",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "429",
                description = "Rate limit exceeded",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
            ApiResponse(
                responseCode = "504",
                description = "PDF generation timeout",
                content = [Content(schema = Schema(implementation = ProblemDetail::class))],
            ),
        ],
    )
    @PostMapping("/generate", produces = ["application/pdf"])
    suspend fun generateResume(
        @Valid
        @OpenApiRequestBody(
            description = "Resume data and template selection",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = GenerateResumeRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Standard Generation",
                            summary = "Generate a resume using the 'modern' template",
                            value = STANDARD_GENERATION_EXAMPLE,
                        ),
                    ],
                ),
            ],
        )
        @RequestBody request: GenerateResumeRequest,
        exchange: ServerWebExchange
    ): ResponseEntity<ByteArray> {
        // Validate payload size (FR-015: reject requests exceeding 100KB)
        val contentLength = exchange.request.headers.contentLength
        when {
            contentLength == -1L -> throw ResponseStatusException(
                HttpStatus.LENGTH_REQUIRED,
                "Content-Length header is required for payload size enforcement (FR-015).",
            )

            contentLength > MAX_BYTES_SUPPORTED -> throw ResponseStatusException(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Request payload too large: $contentLength bytes. Maximum allowed: 102400 bytes (100KB)",
            )
        }

        // Extract locale from Accept-Language header
        val locale = try {
            // Use only the primary language subtag (e.g., "en" from "en-US") to match template names
            val languageCode = exchange.request.headers.acceptLanguage
                .firstOrNull()
                ?.range
                ?.split("-")
                ?.first()
                ?.lowercase()
                ?: "en"

            Locale.from(languageCode)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported locale", e)
        }

        // Get authenticated user ID
        val userId = userIdFromToken()

        // Convert DTO to domain model
        val resumeData = ResumeRequestMapper.toDomain(request)

        // Create command
        val command = GenerateResumeCommand(
            templateId = request.templateId,
            resume = resumeData,
            userId = userId,
            locale = locale,
        )

        // Track generation time
        val startTime = System.currentTimeMillis()

        // Execute command and return PDF
        // Await the result of dispatch (should be suspend)
        val inputStream: InputStream = dispatch(command)
        val pdfBytes = withContext(Dispatchers.IO) {
            inputStream.use { it.readBytes() }
        }
        val generationTimeMs = System.currentTimeMillis() - startTime

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfBytes.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
            .header("X-Generation-Time-Ms", generationTimeMs.toString())
            .header(
                CONTENT_SECURITY_POLICY_HEADER,
                applicationSecurityProperties.contentSecurityPolicy,
            )
            .header(X_CONTENT_TYPE_OPTIONS_HEADER, X_CONTENT_TYPE_OPTIONS_VALUE)
            .header(X_FRAME_OPTIONS_HEADER, X_FRAME_OPTIONS_VALUE)
            .header(REFERRER_POLICY_HEADER, REFERRER_POLICY_VALUE)
            .header(HttpHeaders.CONTENT_LANGUAGE, locale.code)
            .body(pdfBytes)
    }

    companion object {
        private const val CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options"
        private const val X_FRAME_OPTIONS_HEADER = "X-Frame-Options"
        private const val REFERRER_POLICY_HEADER = "Referrer-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff"
        private const val X_FRAME_OPTIONS_VALUE = "DENY"
        private const val REFERRER_POLICY_VALUE = "strict-origin-when-cross-origin"
        private const val MAX_BYTES_SUPPORTED = 100 * 1024
    }
}
