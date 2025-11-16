package com.loomify.resume.infrastructure.http

import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
import com.loomify.resume.application.command.GenerateResumeCommand
import com.loomify.resume.application.handler.GenerateResumeCommandHandler
import com.loomify.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.loomify.resume.infrastructure.http.request.GenerateResumeRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val MAX_BYTES_SUPPORTED = 100 * 1024

/**
 * REST controller for resume generation endpoints.
 */
@RestController
@RequestMapping(value = ["/api/resume"], produces = ["application/vnd.api.v1+json"])
@Validated
class ResumeController(
    private val generateResumeCommandHandler: GenerateResumeCommandHandler,
    private val applicationSecurityProperties: ApplicationSecurityProperties,
) {

    @Operation(summary = "Generate a PDF resume from JSON Resume schema data")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Resume PDF generated successfully",
            content = [Content(mediaType = "application/pdf")],
        ),
        ApiResponse(responseCode = "400", description = "Invalid request data"),
        ApiResponse(responseCode = "422", description = "Business rule validation failed"),
        ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "504", description = "PDF generation timeout"),
    )
    @PostMapping("/generate")
    fun generateResume(
        @Valid @Validated @RequestBody request: GenerateResumeRequest,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ByteArray>> {
        // Validate payload size (FR-015: reject requests exceeding 100KB)
        val contentLength = exchange.request.headers.contentLength
        if (contentLength > MAX_BYTES_SUPPORTED) { // 100KB in bytes
            return Mono.error(
                IllegalArgumentException(
                    "Request payload too large: $contentLength bytes. Maximum allowed: 102400 bytes (100KB)",
                ),
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

            com.loomify.resume.application.command.Locale.from(languageCode)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported locale", e)
        }

        // Convert DTO to domain model
        val resumeData = ResumeRequestMapper.toDomain(request)

        // Create command
        val command = GenerateResumeCommand(resumeData, locale)

        // Track generation time
        val startTime = System.currentTimeMillis()

        // Execute command and return PDF
        return generateResumeCommandHandler.handle(command)
            .map { inputStream ->
                val pdfBytes = inputStream.readBytes()
                val generationTimeMs = System.currentTimeMillis() - startTime

                ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.size.toLong())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                    .header("X-Generation-Time-Ms", generationTimeMs.toString())
                    .header(CONTENT_SECURITY_POLICY_HEADER, applicationSecurityProperties.contentSecurityPolicy)
                    .header(X_CONTENT_TYPE_OPTIONS_HEADER, X_CONTENT_TYPE_OPTIONS_VALUE)
                    .header(X_FRAME_OPTIONS_HEADER, X_FRAME_OPTIONS_VALUE)
                    .header(REFERRER_POLICY_HEADER, REFERRER_POLICY_VALUE)
                    .body(pdfBytes)
            }
    }

    companion object {
        private const val CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options"
        private const val X_FRAME_OPTIONS_HEADER = "X-Frame-Options"
        private const val REFERRER_POLICY_HEADER = "Referrer-Policy"
        private const val X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff"
        private const val X_FRAME_OPTIONS_VALUE = "DENY"
        private const val REFERRER_POLICY_VALUE = "strict-origin-when-cross-origin"
    }
}
