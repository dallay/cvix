package com.loomify.resume.infrastructure.http

import com.loomify.common.domain.bus.Mediator
import com.loomify.engine.authentication.infrastructure.ApplicationSecurityProperties
import com.loomify.resume.application.generate.GenerateResumeCommand
import com.loomify.resume.domain.Locale
import com.loomify.resume.infrastructure.http.mapper.ResumeRequestMapper
import com.loomify.resume.infrastructure.http.request.GenerateResumeRequest
import com.loomify.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

/**
 * REST controller for resume generation endpoints.
 */
@RestController
@RequestMapping(value = ["/api/resume"], produces = ["application/vnd.api.v1+json"])
@Validated
class ResumeGeneratorController(
    mediator: Mediator,
    private val applicationSecurityProperties: ApplicationSecurityProperties,
) : ApiController(mediator) {

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
    @PostMapping("/generate", produces = ["application/pdf"])
    suspend fun generateResume(
        @Valid @Validated @RequestBody request: GenerateResumeRequest,
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

        // Convert DTO to domain model
        val resumeData = ResumeRequestMapper.toDomain(request)

        // Create command
        val command = GenerateResumeCommand(resumeData, locale)

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
            .header(CONTENT_SECURITY_POLICY_HEADER, applicationSecurityProperties.contentSecurityPolicy)
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
